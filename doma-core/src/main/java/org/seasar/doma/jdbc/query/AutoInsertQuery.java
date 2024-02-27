package org.seasar.doma.jdbc.query;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.ArrayList;
import org.seasar.doma.internal.jdbc.entity.AbstractPostInsertContext;
import org.seasar.doma.internal.jdbc.entity.AbstractPreInsertContext;
import org.seasar.doma.internal.jdbc.sql.PreparedSqlBuilder;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.JdbcException;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.SqlKind;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.entity.EntityPropertyType;
import org.seasar.doma.jdbc.entity.EntityType;
import org.seasar.doma.jdbc.entity.GeneratedIdPropertyType;
import org.seasar.doma.jdbc.entity.Property;
import org.seasar.doma.jdbc.id.IdGenerationConfig;
import org.seasar.doma.message.Message;

public class AutoInsertQuery<ENTITY> extends AutoModifyQuery<ENTITY> implements InsertQuery {

  protected boolean nullExcluded;

  protected GeneratedIdPropertyType<ENTITY, ?, ?> generatedIdPropertyType;

  protected IdGenerationConfig idGenerationConfig;

  protected DuplicateKeyType duplicateKeyType = DuplicateKeyType.EXCEPTION;

  public AutoInsertQuery(EntityType<ENTITY> entityType) {
    super(entityType);
  }

  @Override
  public void prepare() {
    super.prepare();
    assertNotNull(method, entityType, entity);
    executable = true;
    preInsert();
    prepareSpecialPropertyTypes();
    prepareOptions();
    prepareTargetPropertyType();
    prepareIdValue();
    prepareVersionValue();
    prepareSql();
    assertNotNull(sql);
  }

  protected void preInsert() {
    AutoPreInsertContext<ENTITY> context =
        new AutoPreInsertContext<>(entityType, method, config, duplicateKeyType);
    entityType.preInsert(entity, context);
    if (context.getNewEntity() != null) {
      entity = context.getNewEntity();
    }
  }

  @Override
  protected void prepareSpecialPropertyTypes() {
    super.prepareSpecialPropertyTypes();
    generatedIdPropertyType = entityType.getGeneratedIdPropertyType();
    if (generatedIdPropertyType != null) {
      idGenerationConfig = new IdGenerationConfig(config, entityType);
      generatedIdPropertyType.validateGenerationStrategy(idGenerationConfig);
      autoGeneratedKeysSupported =
          generatedIdPropertyType.isAutoGeneratedKeysSupported(idGenerationConfig);
    }
  }

  protected void prepareTargetPropertyType() {
    targetPropertyTypes = new ArrayList<>(entityType.getEntityPropertyTypes().size());
    for (EntityPropertyType<ENTITY, ?> propertyType : entityType.getEntityPropertyTypes()) {
      if (!propertyType.isInsertable()) {
        continue;
      }
      Property<ENTITY, ?> property = propertyType.createProperty();
      property.load(entity);
      if (propertyType.isId()) {
        if (propertyType != generatedIdPropertyType
            || generatedIdPropertyType.isIncluded(idGenerationConfig)) {
          targetPropertyTypes.add(propertyType);
        }
        if (generatedIdPropertyType == null && property.getWrapper().get() == null) {
          throw new JdbcException(Message.DOMA2020, entityType.getName(), propertyType.getName());
        }
        continue;
      }
      if (propertyType.isVersion()) {
        targetPropertyTypes.add(propertyType);
        continue;
      }
      if (nullExcluded) {
        if (property.getWrapper().get() == null) {
          continue;
        }
      }
      if (!isTargetPropertyName(propertyType.getName())) {
        continue;
      }
      targetPropertyTypes.add(propertyType);
    }
  }

  protected void prepareIdValue() {
    if (generatedIdPropertyType != null && idGenerationConfig != null) {
      entity = generatedIdPropertyType.preInsert(entityType, entity, idGenerationConfig);
    }
  }

  protected void prepareVersionValue() {
    if (versionPropertyType != null) {
      entity = versionPropertyType.setIfNecessary(entityType, entity, 1);
    }
  }

  protected void prepareSql() {
    Naming naming = config.getNaming();
    Dialect dialect = config.getDialect();
    PreparedSqlBuilder builder = new PreparedSqlBuilder(config, SqlKind.INSERT, sqlLogType);
    if (duplicateKeyType == DuplicateKeyType.EXCEPTION) {
      assembleInsertSql(builder, naming, dialect);
    } else {
      assembleUpsertSql(builder, naming, dialect);
    }
    sql = builder.build(this::comment);
  }

  private void assembleInsertSql(PreparedSqlBuilder builder, Naming naming, Dialect dialect) {
    builder.appendSql("insert into ");
    builder.appendSql(entityType.getQualifiedTableName(naming::apply, dialect::applyQuote));
    builder.appendSql(" (");
    if (!targetPropertyTypes.isEmpty()) {
      for (EntityPropertyType<ENTITY, ?> propertyType : targetPropertyTypes) {
        builder.appendSql(propertyType.getColumnName(naming::apply, dialect::applyQuote));
        builder.appendSql(", ");
      }
      builder.cutBackSql(2);
    }
    builder.appendSql(") values (");
    if (!targetPropertyTypes.isEmpty()) {
      for (EntityPropertyType<ENTITY, ?> propertyType : targetPropertyTypes) {
        Property<ENTITY, ?> property = propertyType.createProperty();
        property.load(entity);
        builder.appendParameter(property.asInParameter());
        builder.appendSql(", ");
      }
      builder.cutBackSql(2);
    }
    builder.appendSql(")");
  }

  private void assembleUpsertSql(PreparedSqlBuilder builder, Naming naming, Dialect dialect) {
    UpsertAssemblerContext context =
        UpsertAssemblerContextBuilder.fromEntity(
            builder,
            entityType,
            duplicateKeyType,
            naming,
            dialect,
            idPropertyTypes,
            targetPropertyTypes,
            entity);
    UpsertAssembler upsertAssemblerQuery = dialect.getUpsertAssembler(context);
    upsertAssemblerQuery.build();
    sql = builder.build(this::comment);
  }

  @Override
  public void generateId(Statement statement) {
    if (generatedIdPropertyType != null && idGenerationConfig != null) {
      entity =
          generatedIdPropertyType.postInsert(entityType, entity, idGenerationConfig, statement);
    }
  }

  @Override
  public void complete() {
    postInsert();
  }

  protected void postInsert() {
    AutoPostInsertContext<ENTITY> context =
        new AutoPostInsertContext<>(entityType, method, config, duplicateKeyType);
    entityType.postInsert(entity, context);
    if (context.getNewEntity() != null) {
      entity = context.getNewEntity();
    }
  }

  public void setNullExcluded(boolean nullExcluded) {
    this.nullExcluded = nullExcluded;
  }

  public void setDuplicateKeyType(DuplicateKeyType duplicateKeyType) {
    this.duplicateKeyType = duplicateKeyType;
  }

  protected static class AutoPreInsertContext<E> extends AbstractPreInsertContext<E> {

    public AutoPreInsertContext(
        EntityType<E> entityType, Method method, Config config, DuplicateKeyType duplicateKeyType) {
      super(entityType, method, config, duplicateKeyType);
    }
  }

  protected static class AutoPostInsertContext<E> extends AbstractPostInsertContext<E> {

    public AutoPostInsertContext(
        EntityType<E> entityType, Method method, Config config, DuplicateKeyType duplicateKeyType) {
      super(entityType, method, config, duplicateKeyType);
    }
  }
}
