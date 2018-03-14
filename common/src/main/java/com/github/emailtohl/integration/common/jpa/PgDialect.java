package com.github.emailtohl.integration.common.jpa;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.type.descriptor.sql.LongVarbinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * 将BLOB按longVarBinary处理，在persistence.xml中用这个类（com.xxx.pgdialect.PgDialect）替换PostgreSQL94Dialect即可。
 */
public class PgDialect extends PostgreSQL94Dialect {
	@Override
	public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		switch (sqlTypeDescriptor.getSqlType()) {
		case Types.CLOB:
			return LongVarcharTypeDescriptor.INSTANCE;
		case Types.BLOB:
			return LongVarbinaryTypeDescriptor.INSTANCE;
		}
		return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
	}
}
