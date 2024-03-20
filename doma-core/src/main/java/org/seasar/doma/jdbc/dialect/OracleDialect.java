package org.seasar.doma.jdbc.dialect;

import org.seasar.doma.expr.ExpressionFunctions;
import org.seasar.doma.jdbc.JdbcMappingVisitor;
import org.seasar.doma.jdbc.SqlLogFormattingVisitor;
import org.seasar.doma.jdbc.id.AutoGeneratedKeysType;
import org.seasar.doma.jdbc.query.UpsertAssembler;
import org.seasar.doma.jdbc.query.UpsertAssemblerContext;

/** A dialect for Oracle Database. */
public class OracleDialect extends Oracle11Dialect {

  public OracleDialect() {
    this(
        new OracleJdbcMappingVisitor(),
        new OracleSqlLogFormattingVisitor(),
        new OracleExpressionFunctions());
  }

  public OracleDialect(JdbcMappingVisitor jdbcMappingVisitor) {
    this(jdbcMappingVisitor, new OracleSqlLogFormattingVisitor(), new OracleExpressionFunctions());
  }

  public OracleDialect(SqlLogFormattingVisitor sqlLogFormattingVisitor) {
    this(new OracleJdbcMappingVisitor(), sqlLogFormattingVisitor, new OracleExpressionFunctions());
  }

  public OracleDialect(ExpressionFunctions expressionFunctions) {
    this(new OracleJdbcMappingVisitor(), new OracleSqlLogFormattingVisitor(), expressionFunctions);
  }

  public OracleDialect(
      JdbcMappingVisitor jdbcMappingVisitor, SqlLogFormattingVisitor sqlLogFormattingVisitor) {
    this(jdbcMappingVisitor, sqlLogFormattingVisitor, new OracleExpressionFunctions());
  }

  public OracleDialect(
      JdbcMappingVisitor jdbcMappingVisitor,
      SqlLogFormattingVisitor sqlLogFormattingVisitor,
      ExpressionFunctions expressionFunctions) {
    super(jdbcMappingVisitor, sqlLogFormattingVisitor, expressionFunctions);
  }

  @Override
  public boolean supportsIdentity() {
    return true;
  }

  @Override
  public boolean supportsAutoGeneratedKeys() {
    return true;
  }

  @Override
  public AutoGeneratedKeysType getAutoGeneratedKeysType() {
    return AutoGeneratedKeysType.FIRST_COLUMN;
  }

  public static class OracleJdbcMappingVisitor extends Oracle11JdbcMappingVisitor {}

  public static class OracleSqlLogFormattingVisitor extends Oracle11SqlLogFormattingVisitor {}

  public static class OracleExpressionFunctions extends Oracle11ExpressionFunctions {

    private static final char[] DEFAULT_WILDCARDS = {'%', '_'};

    public OracleExpressionFunctions() {
      super(DEFAULT_WILDCARDS);
    }

    public OracleExpressionFunctions(char[] wildcards) {
      super(wildcards);
    }

    public OracleExpressionFunctions(char escapeChar, char[] wildcards) {
      super(escapeChar, wildcards);
    }
  }

  public static class OracleScriptBlockContext extends Oracle11ScriptBlockContext {}

  @Override
  public UpsertAssembler getUpsertAssembler(UpsertAssemblerContext context) {
    return new OracleUpsertAssembler(context);
  }
}
