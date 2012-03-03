package org.springframework.jdbc.core;

import org.springframework.jdbc.support.GeneratedKeyHolder;

public class SqlStatements {

    public static PreparedStatementCreator createSqlStatement(String sql, String keyColumn, Object... args) {
        int[] types = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = SqlTypeValue.TYPE_UNKNOWN;
        }
        PreparedStatementCreatorFactory factory = new PreparedStatementCreatorFactory(sql, types);
        factory.setGeneratedKeysColumnNames(new String[] { keyColumn });
        return factory.newPreparedStatementCreator(args);
    }

    public static SqlExecutor use(final JdbcTemplate jdbcTemplate) {
        return new SqlExecutor() {
            public Number insert(String sql, Object... args) {
                return insert(sql, "id", Number.class, args);
            }
            @SuppressWarnings("unchecked")
            public <K> K insert(String sql, String keyColumn, Class<K> keyType, Object... args) {
                GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
                PreparedStatementCreator sqlStmt = createSqlStatement(sql, keyColumn, args);
                jdbcTemplate.update(sqlStmt, keyHolder);
                return (K) keyHolder.getKey();
            }
        };
    }
    
    public interface SqlExecutor {

        Number insert(String sql, Object... args);
        
        <K> K insert(String sql, String keyColumn, Class<K> keyType, Object... args);

    }
    
}
