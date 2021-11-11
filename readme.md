# DB Filter
### General
A module that introduces general filtering functionality. 
Provides a possibility to use it to prepare/fill `PreparedStatement` or as a `Predicate<T>`
where `T` is a type of representing entity. 
The API is mostly described by `QueryTreeParsingService`, `QueryNode` and `ColumnDescription` where:
* `QueryTreeParsingService` parses a java representation of `JsonQuery` and returns `QueryNode`
* `QueryNode` can create a `Predicate<T>`, provide SQL predicate string 
and fill `PreparedStatement` build with predicate string with values
* `ColumnDescription` should be implemented by an enum that describes a single database table.
Each enum value represents a column and provides column name, type of field in an entity, 
and `Function<T, F>` that retrieves field value from an entity (e.g. getter) 

`QueryTreeParsingService` expects that provided `JsonQuery` will contain values with exact type as described in table enum. 
E.g. if there is `BigDecimal.class` in enum value can't be a Double or Integer.

### Usage
#### Sample json query
```json
{
  "expression": "A & (B | C)",
  "predicates": {
    "A": {
      "field": "Size1",
      "value": 100,
      "operator": ">="
    },
    "B": {
      "field": "Size2",
      "value": 10000000,
      "operator": "<"
    },
    "C": {
      "field": "Currency1",
      "value": ["UAH", "EUR"],
      "operator": "IN"
    }
  }
}
```
Where:
* `expression` describes boolean expression that combines predicates
* `predicates` list of column predicates
* `field` name of an enum value (case-sensitive)
* `value` value to compare with
* `operator` one of `IS NULL, IS NOT NULL, IN, NOT IN, =, <>, <, <=, >, >=`

#### Sample enum
```java
public enum Negotiation implements ColumnDescription<NegotiationRow> {

    Id("ID", Long.class, NegotiationRow::getId),
    Product("PRODUCT", String.class, NegotiationRow::getProduct),
    Currency1("CURRENCY1", String.class, NegotiationRow::getCurrency1);

    Negotiation(String cn, Class<?> fieldType, Function<NegotiationRow, ? extends Comparable<?>> fieldAccessor) {
        this.columnName = cn;
        this.fieldType = fieldType;
        this.fieldAccessor = fieldAccessor;
    }

    private final String columnName;
    private final Class<?> fieldType;
    private final Function<NegotiationRow, ? extends Comparable<?>> fieldAccessor;

    public String getColumnName() {
        return this.columnName;
    }

    public Class<?> getFieldType() {
        return this.fieldType;
    }

    public Function<NegotiationRow, ? extends Comparable<?>> getFieldAccessor() {
        return this.fieldAccessor;
    }
}
```
#### Sample usage with PreparedStatement
```java
QueryTreeParsingService parsingService = new QueryParser();
JsonQuery jsonQuery = JsonParsingUtil.parseJson(jsonInput);

QueryNode<T> rootNode = parsingService.parse(jsonQuery, tableEnum);

String sqlPredicate = rootNode.asSqlWhereClause();

StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ")
        .append(TABLE_NAME)
        .append(" WHERE ")
        .append(sqlPredicate);

PreparedStatement ps = connection.prepareStatement(query));

rootNode.fillPreparedStatement(ps)ж
ResultSet rs = ps.executeQuery();
```