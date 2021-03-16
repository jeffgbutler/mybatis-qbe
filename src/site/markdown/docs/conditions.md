# Where Conditions

MyBatis Dynamic SQL supports a wide variety of where clause conditions. All conditions can be combined with "and" and "or" operators to create arbitrarily complex where clauses.

In the following examples:

  * "x" and "y" are values that will be rendered as prepared statement parameters. The resulting SQL is rendered in a format that is compatible with the target runtime (either MyBatis or Spring), but we will show standard prepared statement parameter markers for simplicity.
  * "foo" and "bar" are instances of SqlColumn.

## Simple Conditions

Simple conditions are the most common - they render the basic SQL operators.

| Condition | Example | Result |
|-----------|---------|--------|
| Between | where(foo, isBetween(x).and(y)) | `where foo between ? and ?` |
| Equals | where(foo, isEqualTo(x)) | `where foo = ?` |
| Greater Than | where(foo, isGreaterThan(x)) | `where foo > ?` |
| Greater Than or Equals | where(foo, isGreaterThanOrEqualTo(x)) | `where foo >= ?` |
| In | where(foo, isIn(x, y)) | `where foo in (?,?)` |
| In (case insensitive) | where(foo, isInCaseInsensitive(x, y)) | `where upper(foo) in (?,?)` (the framework will transform the values for x and y to upper case)|
| Less Than | where(foo, isLessThan(x)) | `where foo < ?` |
| Less Than or Equals | where(foo, isLessThanOrEqualTo(x)) | `where foo <= ?` |
| Like | where(foo, isLike(x)) | `where foo like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself) |
| Like (case insensitive) | where(foo, isLikeCaseInsensitive(x)) | `where upper(foo) like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself, the framework will transform the value of x to upper case) |
| Not Between | where(foo, isNotBetween(x).and(y)) | `where foo not between ? and ?` |
| Not Equals | where(foo, isNotEqualTo(x)) | `where foo <> ?` |
| Not In | where(foo, isNotIn(x, y)) | `where foo not in (?,?)` |
| Not In (case insensitive) | where(foo, isNotInCaseInsensitive(x, y)) | `where upper(foo) not in (?,?)` (the framework will transform the values for x and y to upper case)|
| Not Like | where(foo, isLike(x)) | `where foo not like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself) |
| Not Like (case insensitive) | where(foo, isNotLikeCaseInsensitive(x)) | `where upper(foo) not like ?` (the framework DOES NOT add the SQL wild cards to the value - you will need to do that yourself, the framework will transform the value of x to upper case) |
| Not Null | where(foo, isNotNull()) | `where foo is not null` |
| Null | where(foo, isNull()) | `where foo is null` |


## Sub-Selects

Many conditions can be rendered with sub selects.

| Condition | Example | Result |
|-----------|---------|--------|
| Equals | where(foo, isEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo = (select bar from table2 where bar = ?)` |
| Greater Than | where(foo, isGreaterThan(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo > (select bar from table2 where bar = ?)` |
| Greater Than  or Equals | where(foo, isGreaterThanOrEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo >= (select bar from table2 where bar = ?)` |
| In | where(foo, isIn(select(bar).from(table2).where(bar, isLessThan(x))) | `where foo in (select bar from table2 where bar < ?)` |
| Less Than | where(foo, isLessThan(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo < (select bar from table2 where bar = ?)` |
| Less Than  or Equals | where(foo, isLessThanOrEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo <= (select bar from table2 where bar = ?)` |
| Not Equals | where(foo, isNotEqualTo(select(bar).from(table2).where(bar, isEqualTo(x))) | `where foo <> (select bar from table2 where bar = ?)` |
| Not In | where(foo, isNotIn(select(bar).from(table2).where(bar, isLessThan(x))) | `where foo not in (select bar from table2 where bar < ?)` |


## Column Comparison Conditions

Column comparison conditions can be used to write where clauses comparing the values of columns in a table.

| Condition | Example | Result |
|-----------|---------|--------|
| Equals | where(foo, isEqualTo(bar)) | `where foo = bar` |
| Greater Than | where(foo, isGreaterThan(bar)) | `where foo > bar` |
| Greater Than or Equals | where(foo, isGreaterThanOrEqualTo(bar)) | `where foo >= bar` |
| Less Than | where(foo, isLessThan(bar)) | `where foo < bar` |
| Less Than or Equals | where(foo, isLessThanOrEqualTo(bar)) | `where foo <= bar` |
| Not Equals | where(foo, isNotEqualTo(bar)) | `where foo <> bar` |

## Optional Conditions

All conditions support optionality - meaning they can be configured to render into the final SQL if a configured test
passes. Optionality is implemented via standard "filter" and "map" methods - which behave very similarly to the "filter"
and "map" methods in `java.util.Optional`. In general, if a condition's "filter" method is not satisfied, then the
condition will not be rendered. The "map" method can be used the alter the value in a condition before the condition
is rendered.

For example, you could code a search like this:

```java
    public List<AnimalData> searchPeople(String animalName_, Double bodyWeight_, Double brainWeight_) {
        ...
        SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(animalName, isEqualTo(animalName_).filter(Objects::nonNull))
                .and(bodyWeight, isEqualToWhen(bodyWeight_).filter(Objects::nonNull))
                .and(brainWeight, isEqualToWhen(brainWeight_).filter(Objects::nonNull))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        ...
    }
```

In this example, the three conditions will only be rendered if the values passed to them are not null.
If all three values are null, then no where clause will be generated.

Each of the conditions accepts a lambda expression that can be used to determine if the condition should render or not.
The lambdas will all be of standard JDK types (either `java.util.function.BooleanSupplier`,
`java.util.function.Predicate`, or `java.util.function.BiPredicate` depending on the type of condition). The following
table lists the optional conditions and shows how to use them: 

| Condition | Example | Rendering Rules |
|-----------|---------|-----------------|
| Between| where(foo, isBetween(x).and(y).when(BiPredicate)) | The library will pass x and y to the BiPredicate's test method. The condition will render if BiPredicate.test(x, y) returns true |
| Equals | where(foo, isEqualTo(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Greater Than | where(id, isGreaterThan(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Greater Than or Equals | where(id, isGreaterThanOrEqualTo(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Less Than | where(id, isLessThan(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Less Than or Equals | where(id, isLessThanOrEqualTo(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Like | where(id, isLike(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Like Case Insensitive | where(id, isLikeCaseInsensitive(x).when(Predicate&lt;String&gt;)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Not Between | where(id, isNotBetween(x).and(y).when(BiPredicate)) | The library will pass x and y to the BiPredicate's test method. The condition will render if BiPredicate.test(x, y) returns true |
| Not Equals | where(id, isNotEqualTo(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Not Like | where(id, isNotLike(x).when(Predicate)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Not Like Case Insensitive | where(id, isNotLikeCaseInsensitive(x).when(Predicate&lt;String&gt;)) | The library will pass x to the Predicate's test method. The condition will render if Predicate.test(x) returns true |
| Not Null | where(id, isNotNull().when(BooleanSupplier) | The condition will render if BooleanSupplier.getAsBoolean() returns true |
| Null | where(id, isNull().when(BooleanSupplier) | The condition will render if BooleanSupplier.getAsBoolean() returns true |

### "When Present" Condition Builders
The library supplies several methods that supply conditions to be used in the common case of checking for null
values. The table below lists the rendering rules for each of these "when present" condition builder methods.

| Condition | Example | Rendering Rules |
|-----------|---------|-----------------|
| Between| where(foo, isBetweenWhenPresent(x).and(y)) | The condition will render if both x and y values are non-null |
| Equals | where(foo, isEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Greater Than | where(id, isGreaterThanWhenPresent(x)) | The condition will render if x is non-null |
| Greater Than or Equals | where(id, isGreaterThanOrEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Less Than | where(id, isLessThanWhenPresent(x)) | The condition will render if x is non-null |
| Less Than orEquals | where(id, isLessThanOrEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Like | where(id, isLikeWhenPresent(x)) | The condition will render if x is non-null |
| Like Case Insensitive | where(id, isLikeCaseInsensitiveWhenPresent(x)) | The condition will render if x is non-null |
| Not Between | where(id, isNotBetweenWhenPresent(x).and(y)) | The condition will render if both x and y values are non-null |
| Not Equals | where(id, isNotEqualToWhenPresent(x)) | The condition will render if x is non-null |
| Not Like | where(id, isNotLikeWhenPresent(x)) | The condition will render if x is non-null |
| Not Like Case Insensitive | where(id, isNotLikeCaseInsensitiveWhenPresent(x)) | The condition will render if x is non-null |

Note that these methods simply apply a "NotNull" filter to a  condition. For example:

```java
// the following two lines are functionally equivalent
... where (id, isEqualToWhenPresent(x)) ...
... where (id, isEqualTo(x).filter(Objects::nonNull)) ...
```

### Optionality with the "In" Conditions
Optionality with the "in" and "not in" conditions is a bit more complex than the other types of conditions. The first
thing to know is that no "in" or "not in" condition will render if the list of values is empty. For example, there
will never be rendered SQL like `where name in ()`. So optionality of the "in" conditions is more about optionality
of the *values* of the condition. The library comes with functions that will filter out null values, and will upper
case String values to enable case insensitive queries. There are extension points to add additional filtering and 
mapping if you so desire.

We think it is a good thing that the library will not render invalid SQL. An "in" condition will always be dropped from
rendering if the list of values is empty. But there is some danger with this stance. Because the condition could be
dropped from the rendered SQL, more rows could be impacted than expected if the list ends up empty for whatever reason.
Our recommended solution is to make sure that you validate list values - especially if they are coming from direct user
input. Another option is to take some action when the list is empty. This can be especially helpful when you are
applying filters to the value list or using one of the built-in "when present" conditions. In that case, it is
possible that the list of values could end up empty after a validation check.

The "In" conditions support a callback that will be invoked when the value list is empty and just before the statement
is rendered. You could use the callback to create a condition that will throw an exception when the list is empty.
For example:

```java
    private static <T> IsIn<T> isInRequired(Collection<T> values) {
        return IsIn.of(values).withListEmptyCallback(() -> { throw new RuntimeException("In values cannot be empty"); } );
    }

    // Alternatively, there is a convenience method in the Callback interface
    private static <T> IsIn<T> isInRequired(Collection<T> values) {
        return IsIn.of(values).withListEmptyCallback(Callback.exceptionThrowingCallback("In values cannot be empty"));
    }
```

The following table shows the different supplied In conditions and how they will render for different sets of inputs.
The table assumes the following types of input:

- Example 1 assumes an input list of ("foo", null, "bar") - like `where(name, isIn("foo", null, "bar"))`
- Example 2 assumes an input list of (null) - like `where(name, isIn((String)null))`


| Condition | Nulls Filtered | Strings Mapped to Uppercase | Example 1 Rendering | Example 2 Rendering |
|-----------|----------------|--------------------|---------------------|---------------------|
| IsIn| No | No| name in ('foo', null, 'bar') | name in (null) |
| IsInWhenPresent | Yes | No | name in ('foo', 'bar') | No Render |
| IsInCaseInsensitive | No | Yes | upper(name) in ('FOO', null, 'BAR') | upper(name) in (null) |
| IsInCaseInsensitiveWhenPresent | Yes | Yes | upper(name) in ('FOO', 'BAR') | No Render |
| IsNotIn| No | No| name not in ('foo', null, 'bar') | name not in (null) |
| IsNotInWhenPresent | Yes | No | name not in ('foo', 'bar') | No render |
| IsNotInCaseInsensitive | No | Yes | upper(name) not in ('FOO', null, 'BAR') | upper(name) not in (null) |
| IsNotInCaseInsensitiveWhenPresent | Yes | Yes | upper(name) not in ('FOO', 'BAR') | No Render |

If none of these options meet your needs, the "In" conditions also support "map" and "filter" methods for the values. 
This gives you great flexibility to alter or filter the value list before the condition
is rendered.

For example, suppose you wanted to code an "in" condition that accepted a list of strings, but you want to filter out
any null or blank string, and you want to trim all strings. This can be accomplished with code like this:

```java
    SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
            .from(animalData)
            .where(animalName, isIn("  Mouse", "  ", null, "", "Musk shrew  ")
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(st -> !st.isEmpty()))
            .orderBy(id)
            .build()
            .render(RenderingStrategies.MYBATIS3);
```

This code is a bit cumbersome, so if this is a common use case you could build a specialization of the `IsIn` condition
as follows:

```java
import org.mybatis.dynamic.sql.SqlBuilder;

public class MyInCondition {
    public static IsIn<String> isIn(String... values) {
        return SqlBuilder.isIn(values)
               .filter(Objects::nonNull)
               .map(String::trim)
               .filter(st -> !st.isEmpty());
    }
}
```

Then the condition could be used in a query as follows:

```java
    SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
            .from(animalData)
            .where(animalName, MyInCondition.isIn("  Mouse", "  ", null, "", "Musk shrew  "))
            .orderBy(id)
            .build()
            .render(RenderingStrategies.MYBATIS3);
```
