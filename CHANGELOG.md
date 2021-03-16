# Change Log

This log will detail notable changes to MyBatis Dynamic SQL. Full details are available on the GitHub milestone pages.

## Release 1.3.0 - Unreleased

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.3.0+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.3.0+)

### Release Themes

The major themes of this release include the following:

1. Add support for subqueries in select statements - both in a from clause and a join clause.
1. Add support for the "exists" and "not exists" operator. This will work in "where" clauses anywhere
   they are supported.
1. Refactor and improve the built-in conditions for consistency (see below)   
1. Continue to refine the Kotlin DSL. Most changes to the Kotlin DSL are internal and should be source code
   compatible with existing code. There is one breaking change detailed below.
1. Remove deprecated code from prior releases.

### Built-In Condition Refactoring
All built-in conditions have been rafactored. The changes should have no impact for the vast majority of users.
However, there are some changes in behavior and one breaking change.

1. Internally, the conditions no longer hold value Suppliers, they now hold the values themselves. The SqlBuilder
   methods that accept Suppliers will call the `Supplier.get()` method when the condition is constructed. This should
   have no impact unless you were somehow relying on the delay in obtaining a value until the condition was rendered.
1. The existing "then" and "when" methods have been deprecated and replaced with "map" and "filter" respectively.
   The new method names are more familiar and more representative of what these methods actually do. In effect,
   these methods mimic the function of the "map" and "filter" methods on "java.util.Optional" and they are used
   for a similar purpose.
1. The new "filter" method works a bit differently than the "when" method it replaces. The "when" method could not
   be chained - if it was called multiple times, only the last call would take effect. The new "filter" methods works
   as it should and every call will take effect.
1. All the "WhenPresent" conditions have been removed as separate classes. The methods that produced these conditions
   in the SqlBuilder remain, and they will now produce a condition with a "NotNull" filter applied. So at the API level,
   things will function exactly as before, but the intermediate classes will be different.
1. One breaking change is that the builder for List value conditions has been removed without replacement. If you
   were using this builder to supply a "value stream transformer", then the replacement is to build a new List value
   condition and then call the "map" and "filter" methods as needed. For example, prior code looked like this

   ```java
    public static IsIn<String> isIn(String...values) {
        return new IsIn.Builder<String>()
                .withValues(Arrays.asList(values))
                .withValueStreamTransformer(s -> s.filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(st -> !st.isEmpty()))
                .build();
    }
   ```
   New code should look like this:
   ```java
    public static IsIn<String> isIn(String...values) {
        return SqlBuilder.isIn(values)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(st -> !st.isEmpty());
    }
   ```
   We think this is a marked improvement!

### Breaking Change for Kotlin

In this release the Kotlin support for `select` and `count` statements has been refactored. This will not impact code
created by MyBatis generator. It will have an impact on Spring/Kotlin users as well as MyBatis users that coded joins or
other queries directly in Kotlin. The difference is that the `from` clause has been moved inside the lambda for select
and count statements.

Previously, code looked like this:
```kotlin
   val selectStatement = select(foo).from(bar) {
       where(id, isLessThan(3))
   }
```
  
The new code looks like this:
```kotlin
   val selectStatement = select(foo) {
       from(bar)
       where(id, isLessThan(3))
   }
```
  
This change makes the Kotlin DSL a bit more consistent and also makes it easier to implement subquery support in the
Kotlin DSL.

### Added

- Added a new sort specification that is useful in selects with joins ([#269](https://github.com/mybatis/mybatis-dynamic-sql/pull/269))
- Added the capability to generate a camel cased alias for a column ([#272](https://github.com/mybatis/mybatis-dynamic-sql/issues/272))
- Added subquery support for "from" clauses in a select statement ([#282](https://github.com/mybatis/mybatis-dynamic-sql/pull/282))
- Added Kotlin DSL updates to support sub-queries in select statements, where clauses, and insert statements ([#282](https://github.com/mybatis/mybatis-dynamic-sql/pull/282))
- Added subquery support for "join" clauses in a select statement ([#293](https://github.com/mybatis/mybatis-dynamic-sql/pull/293))
- Added support for the "exists" and "not exists" operator in where clauses ([#296](https://github.com/mybatis/mybatis-dynamic-sql/pull/296))
- Refactored the built-in conditions ([#331](https://github.com/mybatis/mybatis-dynamic-sql/pull/331))

## Release 1.2.1 - September 29, 2020

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.2.1+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.2.1+)

### Fixed

- Fixed a bug where the In conditions could render incorrectly in certain circumstances. ([#239](https://github.com/mybatis/mybatis-dynamic-sql/issues/239))

### Added

- Added a callback capability to the "In" conditions that will be called before rendering when the conditions are empty. Also, removed the option that forced the library to render invalid SQL in that case. ([#241](https://github.com/mybatis/mybatis-dynamic-sql/pull/241))
- Added a utility mapper for MyBatis that allows you to run any select query without having to predefine a result mapping. ([#255](https://github.com/mybatis/mybatis-dynamic-sql/pull/255))
- Added utility mappers for MyBatis that allow you to run generic CRUD operations. ([#263](https://github.com/mybatis/mybatis-dynamic-sql/pull/263))

## Release 1.2.0 - August 19, 2020

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.2.0+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.2.0+)

### General Announcements

This release includes major improvements to the Spring support in the library. Spring support is now functionally equivalent to MyBatis support.

This release includes a significant refactoring of the classes in the "org.mybatis.dynamic.sql.select.function" package. The new classes are more consistent and flexible and should be compatible with existing code at the source level (meaning code should be recompiled for the new version of the library). If you have written your own set of functions to extend the library, you will notice that the base classes 'AbstractFunction" and "AbstractMultipleColumnArithmeticFunction" are now deprecated. Their replacement classes are "AbstractUniTypeFunction" and "OperatorFunction" respectively.

With this release, we deprecated several insert methods because they were inconsistently named or awkward. All deprecated methods have documented direct replacements.

All deprecated code will be removed in the next minor release.

### Added

- Added a general insert statement that does not require a separate record class to hold values for the insert. ([#201](https://github.com/mybatis/mybatis-dynamic-sql/issues/201))
- Added the capability to specify a rendering strategy on a column to override the default rendering strategy for a statement. This will allow certain edge cases where a parameter marker needs to be formatted uniquely (for example, "::jsonb" needs to be added to parameter markers for JSON fields in PostgreSQL) ([#200](https://github.com/mybatis/mybatis-dynamic-sql/issues/200))
- Added the ability to write a function that will change the column data type ([#197](https://github.com/mybatis/mybatis-dynamic-sql/issues/197))
- Added the `applyOperator` function to make it easy to use non-standard database operators in expressions ([#220](https://github.com/mybatis/mybatis-dynamic-sql/issues/220))
- Added convenience methods for count(column) and count(distinct column) ([#221](https://github.com/mybatis/mybatis-dynamic-sql/issues/221))
- Added support for union queries in Kotlin ([#187](https://github.com/mybatis/mybatis-dynamic-sql/issues/187))
- Added the ability to write "in" conditions that will render even if empty ([#228](https://github.com/mybatis/mybatis-dynamic-sql/issues/228))
- Many enhancements for Spring including:
  - Fixed a bug where multi-row insert statements did not render properly for Spring ([#224](https://github.com/mybatis/mybatis-dynamic-sql/issues/224))
  - Added support for a parameter type converter for use cases where the Java type of a column does not match the database column type ([#131](https://github.com/mybatis/mybatis-dynamic-sql/issues/131))
  - Added a utility class which simplifies the use of the named parameter JDBC template for Java code - `org.mybatis.dynamic.sql.util.spring.NamedParameterJdbcTemplateExtensions`
  - Added support for general inserts, multi-row inserts, batch inserts in the Kotlin DSL for Spring ([#225](https://github.com/mybatis/mybatis-dynamic-sql/issues/225))
  - Added support for generated keys in the Kotlin DSL for Spring ([#226](https://github.com/mybatis/mybatis-dynamic-sql/issues/226))

## Release 1.1.4 - November 23, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.4+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.4+)

### Added

- Added support for reusing WHERE clauses among count, delete, select, and update statements ([#152](https://github.com/mybatis/mybatis-dynamic-sql/pull/152))
- Improved Kotlin support. Previously, several overloaded methods could collide causing queries to be fragile and very dependent on having the correct imports in a Kotlin file. With this improved support there is no longer any ambiguity. ([#154](https://github.com/mybatis/mybatis-dynamic-sql/pull/154))

### Bugs Fixed

- Fixed issue where limit and offset in sub-queries could cause a parameter name collision ([#142](https://github.com/mybatis/mybatis-dynamic-sql/pull/142))

## Release 1.1.3 - September 16, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.3+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.3+)

### Added

- Added support for `count(distinct ...)` ([#112](https://github.com/mybatis/mybatis-dynamic-sql/issues/112))
- Added support for multiple row inserts ([#116](https://github.com/mybatis/mybatis-dynamic-sql/issues/116))
- Utility classes and a new canonical pattern for MyBatis Generator (CRUD) mappers ([#118](https://github.com/mybatis/mybatis-dynamic-sql/issues/118)) ([#125](https://github.com/mybatis/mybatis-dynamic-sql/pull/125)) ([#128](https://github.com/mybatis/mybatis-dynamic-sql/pull/128))
- Kotlin Extensions and Kotlin DSL ([#133](https://github.com/mybatis/mybatis-dynamic-sql/pull/133)) ([#139](https://github.com/mybatis/mybatis-dynamic-sql/pull/139))

## Release 1.1.2 - July 5, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.2+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.2+)

### Added

- Changed the public SQLBuilder API to accept Collection instead of List for in conditions and batch record inserts. This should have no impact on existing code, but allow for some future flexibility ([#88](https://github.com/mybatis/mybatis-dynamic-sql/pull/88))
- Added the ability to have table catalog and/or schema calculated at runtime. This is useful for situations where there are different database schemas for different environments, or in some sharding situations ([#92](https://github.com/mybatis/mybatis-dynamic-sql/pull/92))
- Add support for paging queries with "offset" and "fetch first" - this seems to be standard on most databases ([#96](https://github.com/mybatis/mybatis-dynamic-sql/pull/96))
- Added the ability to call a builder method on any intermediate object in a select statement and receive a fully rendered statement. This makes it easier to build very dynamic queries ([#106](https://github.com/mybatis/mybatis-dynamic-sql/pull/106))
- Add the ability to modify values on any condition before they are placed in the parameter map ([#105](https://github.com/mybatis/mybatis-dynamic-sql/issues/105))
- Add the ability to call `where()` with no parameters. This aids in constructing very dynamic queries ([#107](https://github.com/mybatis/mybatis-dynamic-sql/issues/107))

## Release 1.1.1 - April 7, 2019

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.1+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.1+)

### Added

- Limit and offset support in the select statement
- Utilities for Spring Batch
- All conditions now support conditional rendering with lambdas
- Select \* support
- Union all support

### Bugs Fixed

- Fixed self joins

## Release 1.1.0 - April 24, 2018

GitHub milestone: [https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.0+](https://github.com/mybatis/mybatis-dynamic-sql/issues?q=milestone%3A1.1.0+)

### Added

- Support for optional conditions
- Support for column comparison conditions
- Support for sub-queries in the update statement
- Support for expressions and constants in the select statement
- Support for function in the update statement

### Bugs Fixed

- Support group by after where

## Initial Release - December 17, 2017
