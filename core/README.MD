# core

## Data Fetchers

To facilitate an extensible way of data fetching the *core* module relies internally on a [CoreDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/IHR2-2318/core/src/main/java/org/dotwebstack/framework/core/datafetchers/CoreDataFetcher.java) interface. This interface extends the GraphQL [DataFetcher](https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/DataFetcher.java) interface. 

```java
public interface CoreDataFetcher<T> extends DataFetcher<T> {

  boolean supports(DataFetchingEnvironment environment);

  DataFetcherType getType();

}
```

The [CoreDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/IHR2-2318/core/src/main/java/org/dotwebstack/framework/core/datafetchers/CoreDataFetcher.java) interface offers the capability to evaluate the support for a given DataFetchingEnvironment with the *supports* method. 

We are currently offering two abstract implementations of the CoreDataFetcher which can be used by anyone. When you write your own *CoreDataFetcher* and annotate it with a Spring *@Component* annotation it will automatically registered.


###[SourceDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/IHR2-2318/core/src/main/java/org/dotwebstack/framework/core/datafetchers/SourceDataFetcher.java)
 
A *SourceDataFetcher* can be used to actually fetch data from *any* source.

###[DelegateDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/IHR2-2318/core/src/main/java/org/dotwebstack/framework/core/datafetchers/DelegateDataFetcher.java)

A *DelegateDataFetcher* can act as a *decorator* for a *SourceDataFetcher* and gives you the capability to transform the result from a *SourceDataFetcher*

