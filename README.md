# Toutiao
   * [Toutiao](#toutiao)
      * [开发工具、主要框架](#开发工具主要框架)
      * [Redis 实现点赞数](#redis-实现点赞数)
      * [ThreadLocal 与 拦截器实现用户鉴别](#threadlocal-与-拦截器实现用户鉴别)
      * [异步消息的发送](#异步消息的发送)
         * [使用枚举类来限定处理的事件的类型。](#使用枚举类来限定处理的事件的类型)
         * [将所有的事件包装为EventType类型。](#将所有的事件包装为eventtype类型)
         * [采用类似于生产者-消费者模式实现异步](#采用类似于生产者-消费者模式实现异步)
            * [生产者](#生产者)
            * [处理器接口](#处理器接口)
            * [消费者](#消费者)
      * [后端接口设计](#后端接口设计)
         * [Return View](#return-view)
         * [Return Json](#return-json)
      * [Bean 生命周期接口](#bean-生命周期接口)
## 开发工具、主要框架
Git + Maven + IDEA + Spring Boot + Velocity + Mybatis

---
## Redis 实现点赞数

Redis的java操作类

```java
@Component
public class JedisAdapter implements InitializingBean{
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisAdapter.class);
    private JedisPool pool = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool();
    }

    private Jedis getJedis() {
        return pool.getResource();
    }

    public long sadd(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sadd(key,value);
        } catch (Exception e) {
            LOGGER.error("发生异常",e);
            return 0;
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
    }
    public long srem(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.srem(key,value);
        } catch (Exception e) {
            LOGGER.error("发生异常",e);
            return 0;
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
    }

    public boolean isMember(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sismember(key,value);
        } catch (Exception e) {
            LOGGER.error("发生异常",e);
            return false;
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
    }

    public long scad(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scard(key);
        } catch (Exception e) {
            LOGGER.error("发生异常",e);
            return 0;
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
    }

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return getJedis().get(key);
        } catch (Exception e) {
            LOGGER.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.set(key, value);
        } catch (Exception e) {
            LOGGER.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setObject(String key, Object obj) {
        set(key, JSON.toJSONString(obj));
    }

    public <T> T getObject(String key) {
        String value = get(key);
        if(value != null) {
            return (T)JSON.parseObject(value);
        }
        return null;
    }


    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            LOGGER.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            LOGGER.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}

```

对每条信息都维护一个Set，Set中放置所有标记了赞的用户Id，并返回所有赞数（集合中用户的个数），更新news表中的被喜欢数，并发送异步“LIKE”事件到队列中。
同时在每次跳转到主页以及页面详情页时都去Redis的集合中查询当前用户是否是集合成员并放入到model传给Velocity模板。

LikeController.java
```java
@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private NewsService newsService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/like"})
    @ResponseBody
    public String like(@RequestParam("newsId") int newsId){
        int userId = hostHolder.get()!=null?hostHolder.get().getId():-1;
        if(userId == -1) {
            return ToutiaoUtil.getJSONString(1,"未登录");
        }
        long likeCount = likeService.like(userId, EntityType.ENTITYTYPE_NEWS,newsId);
        newsService.updateLikeCount(newsId,(int)likeCount);
        News news = newsService.getById(newsId);

        eventProducer.fireEvent(new EventModel(EventType.LIKE).setActorId(hostHolder.get().getId()).setEntityId(newsId)
        .setEntityType(EntityType.ENTITYTYPE_NEWS).setEntityOwnerId(news.getUserId()));
        return ToutiaoUtil.getJSONString(0,String.valueOf(likeCount));
    }


    @RequestMapping(path = {"/dislike"})
    @ResponseBody
    public String dislike(@RequestParam("newsId") int newsId){
        int userId = hostHolder.get().getId();
        long likeCount = likeService.disLike(userId, EntityType.ENTITYTYPE_NEWS,newsId);
        newsService.updateLikeCount(newsId,(int)likeCount);
        return ToutiaoUtil.getJSONString(0,String.valueOf(likeCount));
    }
}

```

HomeController.java
```java
@Controller
public class HomeController {

    @Autowired
    NewsService newsService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;

    @RequestMapping(path = {"/","/index"})
    public String index(Model model, @RequestParam(value = "pop",defaultValue = "0")int pop) {

        model.addAttribute("vos",getNews(0,0,10));
        model.addAttribute("pop",pop);
        return "home";
    }

    @RequestMapping(path = {"/user/{userId}"})
    public String userIndex(Model model,@PathVariable("userId")int userId) {
        model.addAttribute("vos",getNews(userId,0,10));
        return "home";
    }

    @RequestMapping(path = {"/setting"})
    @ResponseBody
    public String settingIndex(Model model,@PathVariable("userId")int userId) {

        return "setting";
    }


    private List<ViewObject> getNews(int userId,int offset,int limit) {
        List<News> newsList = newsService.getLatestNews(userId,offset,limit);
        List<ViewObject> vos = new ArrayList<>(newsList.size());
        int localUserId = hostHolder.get()!=null?hostHolder.get().getId():0;
        for(News news: newsList) {
            ViewObject vo = new ViewObject();
            vo.set("news",news);
            vo.set("user",userService.getUser(news.getUserId()));

            if(localUserId!=0) {
                vo.set("like",likeService.getLikeStatus(localUserId, EntityType.ENTITYTYPE_NEWS,news.getId()));
            } else {
                vo.set("like",0);
            }
            vos.add(vo);


        }
        return vos;
    }
}


```

## ThreadLocal 与 拦截器实现用户鉴别
将ThreadLocal对象包装，每次拦截器生效时，首先查询Cookie是否能有效标识一个用户，若能就放入到ThreadLocal中。

HostHolder.java
```java

@Component
public class HostHolder {

    private final static ThreadLocal<User> threadLocal = new ThreadLocal<User>();


    public  void set(User user) {
        threadLocal.set(user);
    }

    public  User get() {
        return threadLocal.get();
    }

    public void remove() {
        threadLocal.remove();
    }
}
```
在Spring中，拦截器需要实现HandlerInterceptor。
在HandlerInterceptor的三个方法中：
* `public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o)` 会在请求进入Controller之前生效，通过返回值可以决定是否允许请求进入Controller。 在此通过携带的Cookie信息来提取用户信息，若包含有效用户信息则放入HostHolder中，这样可以方便在Controller中获得对应的当前用户信息。
* `void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView)` 当Controller处理之后，返回结果进行渲染之前生效。 在此将当前用户信息放入ModelAndView对象中，便于视图模板的使用。
* `void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e)` 在视图渲染之后生效，在此将当前线程对应的user信息清除，因为该线程下次处理的信息可能不包含用户，如果不清楚就会造成用户信息错乱。

PassPortInterceptor.java
```java

@Component
public class PassPortInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginTicketDao loginTicketDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private HostHolder hostHolder;



    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if(httpServletRequest.getCookies()!=null) {
            for(Cookie cookie : httpServletRequest.getCookies()) {
                if(cookie.getName().equals("ticket")) {
                    ticket = cookie.getValue();

                    break;
                }
            }
        }

        if(ticket != null) {
            LoginTicket loginTicket = loginTicketDao.selectByTicket(ticket);
            if(loginTicket == null || loginTicket.getStatus() !=0 || loginTicket.getExpired().before(new Date())) {
                // cookie 过期
                return true;
            }

            User user = userDao.selectById(loginTicket.getUserId());
            hostHolder.set(user);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && hostHolder.get() != null) {
            modelAndView.addObject("user", hostHolder.get());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.remove();
    }
}
```
当编写一个拦截器后需要在Spring中进行配置。在配置中可以通过制定匹配的路径。

ToutiaoConfiguration.java
```java
@Component
public class ToutiaoConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private PassPortInterceptor passPortInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passPortInterceptor);
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/setting");
        super.addInterceptors(registry);
    }
}
```

## 异步消息的发送
当用户A给用户B点赞后，希望即刻得到反馈的是看到B的赞数增加（有可能加1，也有可能加好多），而对于系统给B发送的 “A点赞了B的某条消息”的这个动作并不要求及时得到响应。因此将类似于不需要及时响应的动作封装放入队列中，有额外的线程专门处理这些动作。

### 使用枚举类来限定处理的事件的类型。
```java
public enum EventType {
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    MAIL(3);

    private int value;
    EventType(int value){this.value = value;}

    public int getValue() {
        return value;
    }
}
```
### 将所有的事件包装为EventType类型。
```java
public class EventModel {
    private EventType type;
    private int actorId;
    private int entityType;
    private int entityId;
    private int entityOwnerId;
    private Map<String, String> exts = new HashMap<String, String>();


    public EventModel(EventType type) {
        this.type = type;
    }

    public EventModel() {}

    public String getExt(String key) {
        return exts.get(key);
    }

    public EventModel setExt(String key,String value) {
        exts.put(key,value);
        return this;
    }
    // 省略其他getter/setter。 每个setter后都 return this; 在设置事件时可以使用连续调用的形式 
    //如： eventProducer.fireEvent(new EventModel(EventType.LIKE).setActorId(hostHolder.get().getId()).setEntityId(newsId).setEntityType(EntityType.ENTITYTYPE_NEWS).setEntityOwnerId(news.getUserId()));
```
### 采用类似于生产者-消费者模式实现异步
采用Redis作为异步队列。生产者放入各种各样的事件，消费者取出每个事件，将事件交付给对应的处理对象。
#### 生产者
```java
@Service
public class EventProducer {

    @Autowired
    JedisAdapter jedisAdapter;

    public boolean fireEvent(EventModel model) {
        try {
            String json = JSON.toJSONString(model);
            String key = RedisKeyUtil.getEventQueueKey();
            jedisAdapter.lpush(key, json);
            return true;
        }catch (Exception e) {
            return false;
        }
    }
}
```

#### 处理器接口
```java
public interface EventHandler {
    void doHandler(EventModel model);

    List<EventType> getSupportEventTypes();
}
```

#### 消费者
消费者从Spring 的AppllicationContext中取出所有继承了EventHandler的bean，根据每个handler能处理的EventType的不同放入到一个Map中，然后从队列中取出事件，分别调用对应的处理器。
```java

@Service
public class EventConsumer implements InitializingBean,ApplicationContextAware{

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);
    private ApplicationContext applicationContext;
    private Map<EventType,List<EventHandler>> config = new HashMap<EventType,List<EventHandler>>();

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String,EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if(beans!=null) {
            for(Map.Entry<String, EventHandler> bean : beans.entrySet()) {
                List<EventType> eventTypes = bean.getValue().getSupportEventTypes();
                for(EventType type : eventTypes) {
                    if(!config.containsKey(type)) {
                        config.put(type,new ArrayList<EventHandler>());
                    }
                    config.get(type).add(bean.getValue());
                    System.out.println(type.toString() + ":" + bean.getValue().toString());
                }
            }
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    String key = RedisKeyUtil.getEventQueueKey();
                    List<String> events = jedisAdapter.brpop(0,key);
                    for(String msg : events) {
                        if(msg.equals(key)) {
                            continue;
                        }
                        EventModel eventModel = JSON.parseObject(msg,EventModel.class);
                        if(!config.containsKey(eventModel.getType())) {
                            LOGGER.error("不能识别的事件");
                            continue;
                        }
                        for(EventHandler handler : config.get(eventModel.getType())) {
                            handler.doHandler(eventModel);
                        }
                    }
                    //System.out.println("???");
                }
            }
        });
        thread.start();
    }
}
```

## 后端接口设计
后端接口有两种，分别是返回视图与返回Json数据。返回视图适用于当需要进行页面跳转等操作，返回Json适用于当只进行数据的传输与返回。
### Return View
举个栗子： 访问首页，此时需要返回首页的页面，再返回页面之前首先要通过数据的查询，将首页所需数据放入置Model中。

```java
@Controller
public class HomeController {

    @Autowired
    NewsService newsService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;

    @RequestMapping(path = {"/","/index"})
    public String index(Model model, @RequestParam(value = "pop",defaultValue = "0")int pop) {

        model.addAttribute("vos",getNews(0,0,10));
        model.addAttribute("pop",pop);
        return "home";
    }

    @RequestMapping(path = {"/user/{userId}"})
    public String userIndex(Model model,@PathVariable("userId")int userId) {
        model.addAttribute("vos",getNews(userId,0,10));
        return "home";
    }



    private List<ViewObject> getNews(int userId,int offset,int limit) {
        List<News> newsList = newsService.getLatestNews(userId,offset,limit);
        List<ViewObject> vos = new ArrayList<>(newsList.size());
        int localUserId = hostHolder.get()!=null?hostHolder.get().getId():0;
        for(News news: newsList) {
            ViewObject vo = new ViewObject();
            vo.set("news",news);
            vo.set("user",userService.getUser(news.getUserId()));

            if(localUserId!=0) {
                vo.set("like",likeService.getLikeStatus(localUserId, EntityType.ENTITYTYPE_NEWS,news.getId()));
            } else {
                vo.set("like",0);
            }
            vos.add(vo);


        }
        return vos;
    }
}
```
### Return Json
当只进行数据的传送只需要改变页面少数部分时可以通过返回Json的方式，如登陆，注册,赞等。

Json操作类
```java
public class ToutiaoUtil {
    private static final Logger logger = LoggerFactory.getLogger(ToutiaoUtil.class);

    public static String getJSONString(int code) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        return json.toJSONString();
    }

    public static String getJSONString(int code, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json.toJSONString();
    }
    // 省略其他util方法...
}
```


## Bean 生命周期接口
* InitializingBean public void afterPropertiesSet() 可以在其中做初始化Bean的操作
* ApplicationContextAware public void setApplicationContext(ApplicationContext applicationContext) 得到ApplicationContext
 
