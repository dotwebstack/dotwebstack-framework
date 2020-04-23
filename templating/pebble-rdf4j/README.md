# 1 pebble-rdf4j

# 1.1 pebble
This templating module includes rdf4j related extensions for Pebble, like filtering.

# 1.2 filtering
In order to filter Json+Ld, use ```jsonld``` filter in the pebble templates. For example, to filter the ```fields```
with jsonld, use:
```html
{% block content %}
<p>{{ fields | jsonld }}</p>
{% endblock %}
```

# 1.3 custom filter
In order to create custom Pebble filter, see: https://pebbletemplates.io/wiki/guide/extending-pebble
