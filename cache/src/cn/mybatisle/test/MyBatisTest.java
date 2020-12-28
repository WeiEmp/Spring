package cn.mybatisle.test;

import cn.mybatisle.bean.Employee;
import cn.mybatisle.dao.EmployeeMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description: 根据xml配置文件（全局配置文件）创建一个SqlSessionFactory对象
 * 1. 接口式编程
 * 原生：      Dao    ===> DaoImpl
 * mybatis:   Mapper ===> xxMapper.xml
 * 2. SqlSession代表和数据库的一次会话，用完必须关闭
 * 3. SqlSession和Connection一样都是非线程安全的，每次使用时都应该获取新的对象
 * 4. mapper接口没有实现类，但是mybatis会为这个接口生成一个代理对象
 *      （将接口和xml进行绑定）
 *      EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
 * 5. 两个重要的配置文件
 *      mybatis的全局配置文件，包含数据库连接池信息，事务管理信息等.....系统运行环境信息
 *      sql映射文件：保存每一个sql语句的映射信息
 *                  将sql抽取出来
 * @author: CodeEmp
 * @time: 2020/12/14 17:34
 */
public class MyBatisTest {
    private SqlSession sqlSession;

    public SqlSessionFactory getSqlSessionFactory() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
        return factory;
    }

    /**
     * 两级缓存:
     * 一级缓存: (本地缓存)：sqlSession级别的缓存。一级缓存是一只开启的；sqlSession级别的一个Map
     *      与数据库同一次会话期间查询到的数据会放在本地缓存中.
     *      以后如果需要获取相同的数据, 直接从缓存中拿,没必要再去查询数据库
     *
     *      一级缓存失效情况(没有使用到当前一级缓存的情况,效果就是,还需要再向数据库发起查询)
     *      1. sqlSession不同.
     *      2. sqlSession相同,查询条件不同(当前一级缓存中没有这个数据)
     *      3. sqlSession相同,两次查询之间执行了增删改操作(这此增删改可能对当前数据有影响)
     *      4. sqlSession相同,手动清除了1级缓存
     * 二级缓存: (全局缓存): 基于namespace级别的缓存：一个namespace对应一个二级缓存
     *         工作机制：
     *         1. 一个会话，查询一条数据：这个数据就会放在当前会话的一级缓存中
     *         2. 如果会话关闭： 一级缓存中的数据会被保存在二级缓存中：新的会话查询信息，就可以参照二级缓存中的内容
     *         3. sqlSession===EmployeeMapper ==> Employee
     *                         DepartmentMapper ==> Department
     *                         不同的namespace查处的数据会放在自己对应的缓存中(map)
     *                         效果：第二次查询会从二级缓存中获取
     *                              查处的数据都会被默认先放在一级缓存中
     *                              只有会话提交或者关闭以后，一级缓存中的数据才会转移到二级缓存中
     *         使用；
     *              1) 开启全局二级缓存配置：<setting name="cacheEnable" value="true" />
     *              2) 去mapper.xml中配置使用二级缓存
     *                  <cache></cache>
 *                  3) POJO需要实现序列化接口
     * 和缓存相关的设置/属性：
     *              1）cacheEnabled=true：false：关闭缓存（二级缓存关闭）（一级缓存可用）
     *              2）每个select标签都有useCache="true"
     *                      false: 不使用缓存（一级缓存仍然可用，二级缓存不可用）
     *              3）每个增删改标签的：flushCache="true"；（一级二级都会清除）
     *                   增删改执行完成后就会清楚缓存
     *                   测试：flushCache="true"       一级缓存就清空 二级也会被清除
     *                   查询标签：flushCache="false"
     *                          如果flushCache=true；每次查询之后都会清空缓存，缓存是没有被使用的
     *              4）sqlSession.clearCache(); 只是清除当前session的一级缓存
     *              5）localCacheScope：本地缓存作用域（一级缓存SESSION）： 当前会话的所有数据保存在会话缓存中
     *                                  STATEMENT：可以禁用一级缓存
     * 第三方缓存整合
     *          1) 导入第三方缓存包即可
     *          2) 导入第三方缓存整合的适配包
     *          3) mapper.xml中使用自定义缓存
     *              <cache type="org.mybatis.caches.ehcache.EhcacheCache">
     *              <cache-ref namespace="cn.mybatisle.dao.EmployeeMapper"/>
     */
    @Test
    public void testSecondLevelCachee() throws IOException {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        SqlSession sqlSession1 = sqlSessionFactory.openSession();

        try {
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
            EmployeeMapper mapper1 = sqlSession1.getMapper(EmployeeMapper.class);

            Employee empById = mapper.getEmpById(1);
            System.out.println(empById);
            sqlSession.close();


            //第二次查询是从二级缓存中拿到的 并没有发送新的sql
            Employee empById1 = mapper1.getEmpById(1);
            System.out.println(empById1);
            sqlSession1.close();
        } finally {

        }
    }

    @Test
    public void testFirstLevelCache() throws IOException {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        SqlSession sqlSession1 = sqlSessionFactory.openSession();

        try {
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
            Employee empById = mapper.getEmpById(1);
            System.out.println(empById);

//            mapper.addEmp(new Employee(null, "jewi", "0", "aefij"));
            //EmployeeMapper mapper1 = sqlSession1.getMapper(EmployeeMapper.class);
            sqlSession.clearCache();
            Employee empById2 = mapper.getEmpById(1);
            System.out.println(empById2 == empById);
        } finally {
            sqlSession.close();
            sqlSession1.close();
        }
    }
}
