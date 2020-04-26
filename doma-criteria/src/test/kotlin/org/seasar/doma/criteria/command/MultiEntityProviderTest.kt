package org.seasar.doma.criteria.command

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.seasar.doma.criteria.entity._Emp
import org.seasar.doma.criteria.entity._NoIdEmp
import org.seasar.doma.criteria.mock.ColumnMetaData
import org.seasar.doma.criteria.mock.MockConfig
import org.seasar.doma.criteria.mock.MockResultSet
import org.seasar.doma.criteria.mock.MockResultSetMetaData
import org.seasar.doma.criteria.mock.RowData
import org.seasar.doma.criteria.query.SelectQuery
import org.seasar.doma.jdbc.PreparedSql
import org.seasar.doma.jdbc.SqlKind
import org.seasar.doma.jdbc.SqlLogType
import org.seasar.doma.jdbc.entity.EntityType

internal class MultiEntityProviderTest {

    private val config = MockConfig()

    private val metaData = MockResultSetMetaData().apply {
        columns.add(ColumnMetaData("id"))
        columns.add(ColumnMetaData("name"))
        columns.add(ColumnMetaData("salary"))
        columns.add(ColumnMetaData("version"))
    }

    private val sql = PreparedSql(SqlKind.SELECT, "", "", "", emptyList(), SqlLogType.FORMATTED)

    private val query = SelectQuery(config, sql)

    @Test
    fun get() {
        val resultSet = MockResultSet(metaData)
        resultSet.rows.add(RowData(1, "hoge", BigDecimal(10000), 100))

        @Suppress("UNCHECKED_CAST")
        val entityType = _Emp() as EntityType<Any>
        val provider = MultiEntityProvider(listOf(entityType), query)

        resultSet.next()
        val multiEntity = provider.get(resultSet)
        assertTrue(multiEntity.keyDataMap.isNotEmpty())
        val keyData = multiEntity.keyDataMap[entityType]
        assertNotNull(keyData)
    }

    @Test
    fun `The keyDataMap should be empty when all column values are null`() {
        val resultSet = MockResultSet(metaData)
        resultSet.rows.add(RowData(null, null, null, null))

        @Suppress("UNCHECKED_CAST")
        val entityType = _Emp() as EntityType<Any>
        val provider = MultiEntityProvider(listOf(entityType), query)

        resultSet.next()
        val multiEntity = provider.get(resultSet)
        assertTrue(multiEntity.keyDataMap.isEmpty())
    }

    @Test
    fun `The entityKeys should be equal when their entity ids are same`() {
        val resultSet = MockResultSet(metaData)
        resultSet.rows.add(RowData(1, "hoge", BigDecimal(10000), 100))
        resultSet.rows.add(RowData(1, "hoge", BigDecimal(10000), 100))

        @Suppress("UNCHECKED_CAST")
        val entityType = _Emp() as EntityType<Any>
        val provider = MultiEntityProvider(listOf(entityType), query)

        resultSet.next()
        val multiEntity1 = provider.get(resultSet)
        assertTrue(multiEntity1.keyDataMap.isNotEmpty())

        resultSet.next()
        val multiEntity2 = provider.get(resultSet)
        assertTrue(multiEntity2.keyDataMap.isNotEmpty())

        val (key1) = multiEntity1.keyDataMap[entityType]!!
        val (key2) = multiEntity2.keyDataMap[entityType]!!
        assertEquals(key1, key2)
    }

    @Test
    fun `The entityKeys should not be equal when their entity has no ids`() {
        val resultSet = MockResultSet(metaData)
        resultSet.rows.add(RowData(1, "hoge", BigDecimal(10000), 100))
        resultSet.rows.add(RowData(1, "hoge", BigDecimal(10000), 100))

        @Suppress("UNCHECKED_CAST")
        val entityType = _NoIdEmp() as EntityType<Any>
        val provider = MultiEntityProvider(listOf(entityType), query)

        resultSet.next()
        val multiEntity1 = provider.get(resultSet)
        assertTrue(multiEntity1.keyDataMap.isNotEmpty())

        resultSet.next()
        val multiEntity2 = provider.get(resultSet)
        assertTrue(multiEntity2.keyDataMap.isNotEmpty())

        val (key1) = multiEntity1.keyDataMap[entityType]!!
        val (key2) = multiEntity2.keyDataMap[entityType]!!
        assertNotEquals(key1, key2)
    }
}
