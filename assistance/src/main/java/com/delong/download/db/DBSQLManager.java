package com.delong.download.db;

class DBSQLManager {

    public static class AppsTable {

        public static final String TABLE_NAME = "apps";

        public enum Column {
            ID("_id", 0),//默认主键
            APP_NAME("app_name", 1),//应用名称
            PKG_NAME("pkg_name", 2),//包名
            VER_CODE("ver_code", 3),//版本号
            CATEGORY("category", 4),//应用类型(应用=1,游戏=2, 游戏和应用=3)
            TASK_ID("task_id", 5),//下载任务id
            DOWNLOAD_URL("download_url", 6),//下载地址
            ICON_URL("icon_url", 7),//icon 地址
            FILE_SIZE("file_size", 8),//文件总大侠
            CURRENT_LENGTH("current_length", 9),//当前下载的大小
            CREATE_TIME("create_time", 10),//插入时间
            CRC("crc", 11),//crc
            MD5("md5", 12),//md5
            APP_ID("app_id", 13),//md5
            APP_STATUS("app_status", 14),//app的状态
            APP_TYPE("app_type", 15)//app类型   配置应用/下载应用/用户收藏/用户删除
            ;
            public final String name;//字段名
            public final int index;//在表中的字段索引,

            /**
             * @param columnName  字段名
             * @param columnIndex 在表中的字段索引,不能错误,修改数据库务必维护该类
             */
            private Column(String columnName, int columnIndex) {
                name = columnName;
                index = columnIndex;
            }

            @Override
            public String toString() {
                return name;
            }

            public static String[] getAllColumns() {
                Column[] columns = Column.values();
                String[] columnsStrs = new String[columns.length];
                for (int i = 0; i < columnsStrs.length; i++) {
                    columnsStrs[i] = columns[i].name;
                }
                return columnsStrs;
            }
        }

        public static String getCreateSQL() {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS ")
                    .append(TABLE_NAME).append("( ")
                    .append(Column.ID.name).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                    .append(Column.APP_NAME.name).append(" TEXT, ")
                    .append(Column.PKG_NAME.name).append(" TEXT UNIQUE, ")
                    .append(Column.VER_CODE.name).append(" LONG, ")
                    .append(Column.CATEGORY.name).append(" INTEGER, ")
                    .append(Column.TASK_ID.name).append(" INTEGER, ")
                    .append(Column.DOWNLOAD_URL.name).append(" TEXT, ")
                    .append(Column.ICON_URL.name).append(" TEXT, ")
                    .append(Column.FILE_SIZE.name).append(" LONG, ")
                    .append(Column.CURRENT_LENGTH.name).append(" LONG, ")
                    .append(Column.CREATE_TIME.name).append(" LONG,")
                    .append(Column.CRC.name).append(" TEXT,")
                    .append(Column.MD5.name).append(" TEXT,")
                    .append(Column.APP_ID.name).append(" INTEGER,")
                    .append(Column.APP_STATUS.name).append(" INTEGER,")
                    .append(Column.APP_TYPE.name).append(" INTEGER")
                    .append(");");
            return sb.toString();
        }

        public static String getDropSQL() {
            return "DROP TABLE IF EXISTS " + TABLE_NAME;
        }
    }
}
