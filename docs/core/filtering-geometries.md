# Filtering optimized for large geometries
For performance reasons is can be useful to split large geometries into smaller segments. This can be realized by 
plotting a geometry on a fixed tile grid with tiles of 10 x 10 km. This tile grid is generated once as in the example 
underneath:

'''sql
CREATE TABLE IF NOT EXISTS public.tiles_10km AS
SELECT x || '_' || y                                        AS tile_id,
-- Maak rechthoek polygoon
        ST_MakeEnvelope(x, y, x + 10000, y + 10000, 28992)  AS geom_rd
-- van linksonder: x = (-41171,606), y = (306846,073) in stappen van 10km (10000)
FROM generate_series(-41200, 306000, 10000) x
-- van rechtsboven x = (278026,057), y = (866614,784) in stappen van 10km (10000)
    CROSS JOIN generate_series(280000, 870000, 10000) y;

CREATE INDEX IF NOT EXISTS tiles_10km_sdx1 ON public.tiles_10km USING GIST (geom_rd);
'''
If you want to split a large geometry into segments according to the fixed tile grid, you need an extra 'segments' table.
This table is automatically updated during mutation of the geometry. When making use of a filter based on a geometry,
dotwebstack will use the 'segments' table to create the filter conditon. 

The segment table must conform to the following conventions: <source table>__<geometry column>__segments
- the segments table is prefixed by the name of the table which contains the geometry that needs to be split 
  into segments
- the middle part of the segments table is the name of the geometry column
- the segments table has the postfix 'segments'

The 'segments' table has at least three columns
- tile_id: is a referenceto the 'tiles_10km' table
- <geometrie column>: the name of the geometry column
- the other columns refer to the primary key(s) of the source table

- Example: the table 'brewery' has a column 'geometrie' which is split into segments. 

| brewery         |         
|-----------------|
| identifier (PK) |
| geometry        |
|                 |

The table above results in the 'segments' table as shown underneath:

| brewery__geometry__segments |
|-----------------------------|
| tile_id                     |
| geometry                    |
| identifier                  |


If the segments table conforms to the above conventions, dotwebstack will use the segments table when a geometry filter 
is applied. 
