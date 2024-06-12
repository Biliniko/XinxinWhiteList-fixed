package ltd.dreamcraft.xinxinwhitelist.database;

public class MySQL {

//
//  public static void setUP() {
//    ENABLED = true;
//    FileConfiguration cfg = XinxinWhiteList.getMysqlSettings().getConfig();
//    String driver = "com.mysql.cj.jdbc.Driver";
//    try {
//      Class.forName(driver);
//    } catch (Exception ignored) {
//      driver = "com.mysql.jdbc.Driver";
//      XinxinWhiteList.getInstance().getLogger().info("Driver class '" + driver + "' not found! Falling back to legacy MySQL driver (com.mysql.jdbc.Driver)");
//    }
//    String jdbcUrl = "jdbc:mysql://" + cfg.getString("storage.host") + ':' + cfg.getString("storage.port") + '/' + cfg.getString("storage.database");
//    DATABASE = cfg.getString("storage.database");
//    TABLE = cfg.getString("storage.table");
//    Properties properties = new Properties();
//    properties.setProperty("user", cfg.getString("storage.username"));
//    properties.setProperty("password", cfg.getString("storage.password"));
//    properties.setProperty("useSSL", cfg.getString("storage.useSSL"));
//    properties.setProperty("date_string_format", "yyyy-MM-dd HH:mm:ss");
//    try {
//      Connection connection = DriverManager.getConnection(jdbcUrl, properties).var;
//      createTables(connection);
//      if (!hasData()) {
//        XinxinWhiteList.getInstance().getLogger().info("??c????л???MYSQL???棬????δ???κ????????????yaml????....");
//        FileConfiguration data = XinxinWhiteList.getPlayerData().getConfig();
//        int imported = 0;
//        for (String name : data.getConfigurationSection("").getKeys(false)) {
//          String qq = data.getString(name);
//          imported++;
//          savePlayer(Long.valueOf(Long.parseLong(qq)), name);
//        }
//        XinxinWhiteList.getInstance().getLogger().info("??c???YAML???浼????" + imported + "???????");
//      }
//    } catch (SQLException e) {
//      e.printStackTrace();
//    }
//  }
//
//
//  public static void createTables() throws SQLException {
//    String file = "/create.sql";
//    try(InputStream in = XinxinWhiteList.getInstance().getClass().getResourceAsStream("/create.sql");
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
//        Connection con = dataSource.getConnection();
//        Statement stmt = con.createStatement()) {
//      StringBuilder builder = new StringBuilder();
//      String line;
//      while ((line = reader.readLine()) != null) {
//        if (line.startsWith("#"))
//          continue;
//        builder.append(line);
//        if (!line.endsWith(";"))
//          continue;
//        String sql = builder.toString();
//        stmt.addBatch(String.format(sql, new Object[] { TABLE }));
//        builder = new StringBuilder();
//      }
//      stmt.executeBatch();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  public static boolean hasData() {
//    String sql = String.format("SELECT * FROM `%s` LIMIT 1;", new Object[] { TABLE });
//    try(Connection con = dataSource.getConnection();
//        PreparedStatement stmt = con.prepareStatement(sql);
//        ResultSet resultSet = stmt.executeQuery()) {
//      if (resultSet.next())
//        return true;
//    } catch (SQLException sqlEx) {
//      sqlEx.printStackTrace();
//    }
//    return false;
//  }
//
//  public static void removePlayer(Long qq) {
//    String sql = String.format("DELETE FROM `%s` WHERE `qq`=?;", new Object[] { TABLE });
//    try(Connection con = dataSource.getConnection();
//        PreparedStatement stmt = con.prepareStatement(sql, 1)) {
//      stmt.setLong(1, qq.longValue());
//      stmt.executeUpdate();
//    } catch (SQLException sqlEx) {
//      sqlEx.printStackTrace();
//    }
//  }
//
//  public static void removePlayer(String name) {
//    String sql = String.format("DELETE FROM `%s` WHERE `name`=?;", new Object[] { TABLE });
//    try(Connection con = dataSource.getConnection();
//        PreparedStatement stmt = con.prepareStatement(sql, 1)) {
//      stmt.setString(1, name);
//      stmt.executeUpdate();
//    } catch (SQLException sqlEx) {
//      sqlEx.printStackTrace();
//    }
//  }
//
//  public static void savePlayer(Long qq, String name) {
//    String sql = String.format("INSERT INTO `%s` (`qq`, `name`)VALUES(?,?)  ON DUPLICATE KEY UPDATE `name`=?;", new Object[] { TABLE });
//    try(Connection con = dataSource.getConnection();
//        PreparedStatement stmt = con.prepareStatement(sql, 1)) {
//      stmt.setLong(1, qq.longValue());
//      stmt.setString(2, name);
//      stmt.setString(3, name);
//      stmt.executeUpdate();
//    } catch (SQLException sqlEx) {
//      sqlEx.printStackTrace();
//    }
//  }
//
//  public static void modifyPlayer(String name, Long qq) {
//    String sql = String.format("UPDATE `%s` SET `qq`=? WHERE `name`=?;", new Object[] { TABLE });
//    try(Connection con = dataSource.getConnection();
//        PreparedStatement stmt = con.prepareStatement(sql)) {
//      stmt.setLong(1, qq.longValue());
//      stmt.setString(2, name);
//      stmt.executeUpdate();
//    } catch (SQLException sqlEx) {
//      sqlEx.printStackTrace();
//    }
//  }
//
//  public static String getPlayer(Long qq) {
//    String sql = String.format("SELECT `id`, `qq`, `name` FROM `%s` WHERE `qq`=?;", new Object[] { TABLE });
//    try(Connection con = dataSource.getConnection();
//        PreparedStatement stmt = con.prepareStatement(sql)) {
//      stmt.setLong(1, qq.longValue());
//      try (ResultSet resultSet = stmt.executeQuery()) {
//        if (resultSet.next())
//          return resultSet.getString("name");
//      }
//    } catch (SQLException sqlEx) {
//      sqlEx.printStackTrace();
//    }
//    return null;
//  }
//
//  public static Long getQQ(String name) {
//    String sql = String.format("SELECT `id`, `qq`, `name` FROM `%s` WHERE `name`=?;", new Object[] { TABLE });
//    try(Connection con = dataSource.getConnection();
//        PreparedStatement stmt = con.prepareStatement(sql)) {
//      stmt.setString(1, name);
//      try (ResultSet resultSet = stmt.executeQuery()) {
//        if (resultSet.next())
//          return Long.valueOf(resultSet.getLong("qq"));
//      }
//    } catch (SQLException sqlEx) {
//      sqlEx.printStackTrace();
//    }
//    return null;
//  }
//
//  public static void close() {
//    if (dataSource != null) {
//      try {
//        dataSource.close();
//      } catch (SQLException e) {
//        throw new RuntimeException(e);
//      }
//    }
//  }
//
//  public static boolean ENABLED = false;
//
//  private static Connection dataSource;
//
//  private static String DATABASE;
//
//  private static String TABLE;

}

