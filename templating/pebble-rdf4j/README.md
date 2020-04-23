# 1 pebble-rdf4j

# 1.1 pebble
This templating module uses the pebble templating implementation. See https://pebbletemplates.io for documentation on pebble.

# 1.2 template file location
In order to create your own template files, create these in ```config/templates/```.

# 1.3 template file variable references
For more information, see the [templating-pebble docs](../pebble/README.md)

# 1.4 filtering
In order to filter Json+Ld, use ```jsonld``` filter in the pebble templates. For example, to filter the ```fields```
with jsonld, use:
```html
{% block content %}
<p>{{ fields | jsonld }}</p>
{% endblock %}
```

# 1.5 custom filter
In order to create custom Pebble filter, see: https://pebbletemplates.io/wiki/guide/extending-pebble
