package org.seasar.doma.jdbc.dialect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.expr.ExpressionFunctions;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.JdbcMappingVisitor;
import org.seasar.doma.jdbc.ScriptBlockContext;
import org.seasar.doma.jdbc.SelectForUpdateType;
import org.seasar.doma.jdbc.SelectOptions;
import org.seasar.doma.jdbc.Sql;
import org.seasar.doma.jdbc.SqlLogFormatter;
import org.seasar.doma.jdbc.SqlLogFormattingVisitor;
import org.seasar.doma.jdbc.SqlNode;
import org.seasar.doma.jdbc.criteria.query.CriteriaBuilder;
import org.seasar.doma.jdbc.id.AutoGeneratedKeysType;
import org.seasar.doma.jdbc.query.UpsertAssembler;
import org.seasar.doma.jdbc.query.UpsertAssemblerContext;
import org.seasar.doma.jdbc.type.JdbcType;
import org.seasar.doma.wrapper.Wrapper;

/**
 * A {@literal RDBMS} dialect.
 *
 * <p>This interface absorb the difference between {@literal RDBMS}s.
 *
 * <p>The implementation instance must be thread safe.
 */
public interface Dialect {

  /**
   * The dialect name.
   *
   * @return the name
   */
  String getName();

  /**
   * Transforms the SQL node.
   *
   * @param sqlNode the SQL node
   * @param options the options
   * @return the transformed node
   * @throws DomaNullPointerException if any argument is {@code null}
   * @throws JdbcException if unsupported options are specified
   */
  SqlNode transformSelectSqlNode(SqlNode sqlNode, SelectOptions options);

  /**
   * Transforms the SQL node to get a row count.
   *
   * @param sqlNode the SQL node
   * @return the transformed node
   * @throws DomaNullPointerException if any argument is {@code null}
   */
  SqlNode transformSelectSqlNodeForGettingCount(SqlNode sqlNode);

  /**
   * Whether the {@code sqlException} represents an unique violation.
   *
   * @param sqlException the SQL exception
   * @return {@code true}, if the {@code sqlException} represents an unique violation
   * @throws DomaNullPointerException if {@code sqlException} is {@code null}
   */
  boolean isUniqueConstraintViolated(SQLException sqlException);

  /**
   * Whether this object includes the IDENTITY column in SQL INSERT statements.
   *
   * @return {@code true}, if this object includes it
   */
  boolean includesIdentityColumn();

  /**
   * Whether this object supports the IDENTITY column.
   *
   * @return {@code true}, if this object supports it
   */
  boolean supportsIdentity();

  /**
   * Whether this object supports the SEQUENCE.
   *
   * @return {@code true}, if this object supports it
   */
  boolean supportsSequence();

  /**
   * Whether this object supports {@link Statement#getGeneratedKeys()}.
   *
   * @return {@code true}, if this object supports it
   */
  boolean supportsAutoGeneratedKeys();

  /**
   * Whether this object supports the result of {@link Statement#executeBatch()}.
   *
   * @return {@code true}, if this object supports it
   */
  boolean supportsBatchUpdateResults();

  default boolean supportsBatchExecutionReturningGeneratedValues() {
    return false;
  }

  /**
   * Whether this object supports pessimistic locking.
   *
   * @param type a kind of pessimistic locking
   * @param withTargets {@code true} if a lock target is specified
   * @return {@code true}, if this object supports it
   */
  boolean supportsSelectForUpdate(SelectForUpdateType type, boolean withTargets);

  /**
   * Whether this object supports an out parameter that is mapped to a result set in stored
   * functions or stored procedures .
   *
   * @return {@code true}, if this object supports it
   */
  boolean supportsResultSetReturningAsOutParameter();

  /**
   * Whether this object supports a reservation of identity.
   *
   * @return {@code true}, if this object supports it
   */
  boolean supportsIdentityReservation();

  /**
   * Whether this object supports alias reference in DELETE clause as follows:
   *
   * <pre>
   * DELETE t FROM employee t
   * </pre>
   *
   * @return {@code true}, if this object supports it
   */
  default boolean supportsAliasInDeleteClause() {
    return false;
  }

  /**
   * Whether this object supports alias reference in UPDATE clause as follows:
   *
   * <pre>
   * UPDATE t SET t.age = 30 FROM employee t
   * </pre>
   *
   * @return {@code true}, if this object supports it
   */
  default boolean supportsAliasInUpdateClause() {
    return false;
  }

  /**
   * Whether this object supports mod operator {@code %}.
   *
   * @return {@code true}, if this object supports it
   */
  default boolean supportsModOperator() {
    return true;
  }

  default boolean supportsMultiRowInsertStatement() {
    return true;
  }

  default boolean supportsAutoIncrementWhenInsertingMultipleRows() {
    return true;
  }

  /**
   * Returns an SQL object to get IDENTITY values that are generated in the database.
   *
   * <p>This method is available, only if {@link #supportsIdentity()} returns {@code true}.
   *
   * @param catalogName the catalog name
   * @param schemaName the schema name
   * @param tableName the table name
   * @param columnName the IDENTITY column name
   * @param isQuoteRequired whether the quotation marks are required
   * @param isIdColumnQuoteRequired whether the quotation marks are required for the IDENTITY column
   * @return the SQL object
   * @throws DomaNullPointerException if either the {@code tableName} or the {@code columnName} is
   *     {@code null}
   */
  Sql<?> getIdentitySelectSql(
      String catalogName,
      String schemaName,
      String tableName,
      String columnName,
      boolean isQuoteRequired,
      boolean isIdColumnQuoteRequired);

  /**
   * Returns an SQL object to reserve identity in the database.
   *
   * <p>This method is available, only if {@link #supportsIdentityReservation()} returns {@code
   * true}.
   *
   * @param catalogName the catalog name
   * @param schemaName the schema name
   * @param tableName the table name
   * @param columnName the IDENTITY column name
   * @param isQuoteRequired whether the quotation marks are required
   * @param isIdColumnQuoteRequired whether the quotation marks are required for the IDENTITY column
   * @param reservationSize the size of the reservation
   * @return the SQL object
   * @throws DomaNullPointerException if either the {@code tableName} or the {@code columnName} is
   *     {@code null}
   */
  Sql<?> getIdentityReservationSql(
      String catalogName,
      String schemaName,
      String tableName,
      String columnName,
      boolean isQuoteRequired,
      boolean isIdColumnQuoteRequired,
      int reservationSize);

  /**
   * Returns an SQL object to get next sequence values.
   *
   * <p>This method is available, only if {@link #supportsSequence()} returns {@code true}.
   *
   * @param qualifiedSequenceName the qualified sequence name
   * @param allocationSize the allocation size of the sequence
   * @return the SQL object
   * @throws DomaNullPointerException if {@code qualifiedSequenceName} is {@code null}
   */
  Sql<?> getSequenceNextValSql(String qualifiedSequenceName, long allocationSize);

  /**
   * Returns the {@link JdbcType} object that corresponds to the {@link ResultSet} class.
   *
   * <p>This method is available, only if {@link #supportsResultSetReturningAsOutParameter()} is
   * {@code true}.
   *
   * @return the {@link JdbcType} object for the {@link ResultSet} class
   */
  JdbcType<ResultSet> getResultSetType();

  /**
   * Enclose the name with quotation marks.
   *
   * @param name the name of a database object such as a table, a column, and so on
   * @return the name that is enclosed with quotation marks
   */
  String applyQuote(String name);

  /**
   * Remove quotation marks from the name
   *
   * @param name the name of a database object such as a table, a column, and so on
   * @return the name that has no enclosing quotation marks
   */
  String removeQuote(String name);

  /**
   * Returns the root cause of the SQL exception.
   *
   * @param sqlException the SQL exception
   * @return the root cause
   * @throws DomaNullPointerException if the {@code sqlException} is {@code null}
   */
  Throwable getRootCause(SQLException sqlException);

  /**
   * Returns the visitor that maps {@link Wrapper} to {@link JdbcType}.
   *
   * @return the visitor
   */
  JdbcMappingVisitor getJdbcMappingVisitor();

  /**
   * Return the visitor that maps {@link Wrapper} to {@link SqlLogFormatter}.
   *
   * @return the visitor
   */
  SqlLogFormattingVisitor getSqlLogFormattingVisitor();

  /**
   * Returns the aggregation of the expression functions that are available in the SQL templates.
   *
   * @return the aggregation of the expression functions
   */
  ExpressionFunctions getExpressionFunctions();

  /**
   * Creates the context object to process an SQL block in a script.
   *
   * @return the context object
   */
  ScriptBlockContext createScriptBlockContext();

  /**
   * Return the delimiter that is used as the end of an SQL block in a script.
   *
   * @return the delimiter
   */
  String getScriptBlockDelimiter();

  /**
   * Returns the type of the auto generated keys.
   *
   * @return the type of the auto generated keys
   */
  AutoGeneratedKeysType getAutoGeneratedKeysType();

  CriteriaBuilder getCriteriaBuilder();

  /**
   * Returns the UpsertAssembler implementation for the given context.
   *
   * @param context the UpsertAssemblerContext object
   * @return the UpsertAssembler object for the given context
   */
  UpsertAssembler getUpsertAssembler(UpsertAssemblerContext context);
}
