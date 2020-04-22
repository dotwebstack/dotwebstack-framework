# 1 pebble templating

# 1.1 pebble
This templating module uses the pebble templating implementation. See https://pebbletemplates.io for documentation on pebble.

# 1.2 template file location
In order to create your own template files, create these in ```config/templates/```.

# 1.3 template file variable references
For these implementation, we have prefixed different values regarding the source of the values.
Values that are part of the result fields of the GraphQL query, are prefixed with: ```fields.```<br/>
Values that are part of the input arguments of the GrapQL query, are prefixed with: ```args.``` <br/>
Values that are part of the environment variables, are prefixed with: ```env.```<br/>

For example, in the example project we are referring to breweries. The name of the brewery is part of the result fields, as key ```name```.
In order to use this in the pebble template, we need to use ```{{ fields.name }}```
