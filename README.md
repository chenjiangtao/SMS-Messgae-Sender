
# **SMS Web Message Sender**

## **HTTP的提交与推送**

*	msgid
	短消息标志，由服务器生成，在用户提交短信后返回给客户端,一条短信的msgid是唯一的一条长（超
        过70个中文字符）按其被拆分的短信条数返回数个msgid,用户可以使用msgid查询该短信的状态。

*	客户端提交类型
     -   短信发送
     - 查询状态报告，允许每30秒查询一次
     -	查询短信回复，允许每10秒查询一次
     - 提交方式为HTTP协议GET请求
*	提交参数定义
*	短信提交：
     -	服务映射名：HttpSendSM
     -	帐号（acsrccode），即企业帐号或子帐号
     -	密码（pswd）
     -	手机号（mobile），只能包含1个手机号。   
     -	短信内容（msg），短信内容长度不能超过585个字符。使用URL  方式编码为UTF格式
     -	是否需要状态报告（needstatus），取值true或false   
     - [例子](http://222.66.24.235/ctmp/HttpSendSM?acsrccode=10658939312345&pswd=12345&mobile=13564900192&msg=hello&needstatus=true) 

			
	查询状态报告：
1)	服务映射名：HttpQueryReport
2)	帐号(acsrccode)，即企业帐号或子帐号
3)	密码(pswd)
4)	消息号(msgid)
5)	开始时间(start)，格式yyyymmddhhmmss
6)	结束时间(end)，格式yyyymmddhhmmss
7)	查询标志(flag)，取值1表示按msgid查询，2表示按时间段查询
8)	例子：
http://222.66.24.235/ctmp/HttpQueryReport?acsrccode=10658939312345&pswd=12345&msgid=12345&start=20090211060000&end=20090211080000&flag=1
				
	查询短信回复：
1)	服务映射名：HttpQueryDeliver
2)	帐号(acsrccode) ，即企业帐号或子帐号
3)	密码(pswd)
4)	开始时间(start) ，格式yyyymmddhhmmss
5)	结束时间(end) ，格式yyyymmddhhmmss
6)	查询结果是否删除的标记(indicate)，true删除，false保留
7)	是否包含子帐号短信回复(all)，true 包含，false 不包含
8)	例子：
http://222.66.24.235/ctmp/HttpQueryDeliver?acsrccode=10658939312345&pswd=12345&start=20090212000000&end=20090212080000&indicate=true

	注：
1)	查询起始时间据当天不超过2天
2)	查询开始时间和结束时间要在一个自然天内
3)	用msgid查询，如果该消息距当天超过三天，则不返回结果
4)	时间格式：yyyymmddhhmmss
5)	all标志只在acsrccode为企业主帐号时有效
		

4.	短信提交响应定义
	提交成功的短信，返回一个提交响应，其中包含的msgid个数与拆分的条数一致。
	提交失败的短信，返回一个包含相应错误代码的提交响应。
	字段：
1)	resptime
2)	respstatus
3)	msgids

	消息格式：
resptime,respstatus
msgid1
msgid2

5.	状态报告查询响应定义
	字段：
1)	resptime
2)	respstatus
3)	msgid      短信标志
4)	reporttime   手机收到短信的时间
5)	mobile      手机号码
6)	reportstatus  状态报告值

	消息格式： 
resptime,respstatus
msgid1,reporttime1,mobile1,reportstatus1
msgid2,reporttime2,mobile2,reportstatus2
………………

6.	短信回复查询响应定义
	字段：
1)	resptime
2)	respstatus
3)	delivertime   接收时间
4)	mobile       手机号码
5)	msg         使用URL方式编码为UTF-8格式
6)	acsrccode     企业子帐号

	消息格式：
resptime,respstatus
delivertime1,mobile1,msg1,acsrccode
delivertime2,mobile2,msg2,acsrccode
…………

7.	短信提交流程
	登录验证
	发送方式验证：是否允许提交http消息
	IP验证
	提交速度控制
	参数验证
1)	手机号是否存在
2)	去除相同手机号
3)	手机号是否过多
4)	是否包含无效手机号
5)	短信内容是否超长
6)	敏感词检验
	上述验证过程通过后，消息入队列及数据库

8.	查询状态报告流程
*	登录验证
*	发送方式验证：是否允许提交http消息
	IP验证
	提交速度控制
	参数验证
1)	是否包含查询类型字段
2)	若根据msgid查询，检查msgid是否存在
3)	若根据时间查询，检查时间是否存在以及时间格式是否合法
*	根据查询条件在数据库中查询状态报告
*	将查询所得的所有状态报告构造成一定格式的数据，发送给客户端

9.	查询短信回复流程
*	登录验证
*	发送方式验证：是否允许提交http消息
*	IP验证
*	提交速度控制
*	参数验证：验证时间是否存在以及时间格式是否合法
*	根据时间段，在数据库查询短信回复
*	根据是否需要删除查询结果的标识，做相应处理
*	将查询所得构造成一定格式的数据，发送给客户端

10.	提交流速控制
	每家企业都有流速控制，即每秒可以提交的最大手机号码个数
	流控原则：最大速度提交时强制匀速；低速提交无匀速限制
	根据企业提交速度，计算出相邻两次提交之间的时间T
	当一个提交到达时，请求令牌（token）。如果令牌状态为“可用”，则进行后续处理，并将令牌状态修改为“不可用”；否则返回“提交过快”的响应。有一个线程每隔时间T将令牌状态置为“可用”
	短信提交、状态报告查询和mo查询均有速度控制：
1)	短信提交：根据企业流速进行控制
2)	状态报告查询和mo查询：平台统一查询速度为1条/30秒
3)	对于所有的提交、查询等，系统会根据处理能力和运行状况设定一个总的并发阀值，当所有企业提交总和超过阀值会返回系统忙
	三种提交方式互不干扰，即每种提交方式拥有自己token

11.	状态报告与短信回复推送
	状态报告：
1)	接收者（receiver）
2)	密码（pswd）
3)	消息号（msgid）
4)	时间（reportTime）：格式YYMMDDhhmm，其中YY=年份的最后两位（00-99），MM=月份（01-12），DD=日（01-31），hh=小时（00-23），mm=分钟（00-59）
5)	手机（mobile）
6)	状态（status）
7)	标识（isReport）
8)	例子：
http://registerURL?receiver=admin&pswd=12345&msgid=12345&reportTime=1012241002&mobile=13564900192&status=DELIVRD&isReport=true
			
	短信回复：
1)	接收者（receiver）
2)	密码（pswd）
3)	时间（moTime）：格式YYMMDDhhmm，其中YY=年份的最后两位（00-99），MM=月份（01-12），DD=日（01-31），hh=小时（00-23），mm=分钟（00-59）
4)	手机（mobile）
5)	内容（msg）
6)	例子：
http://registerURL?receiver=admin&pswd=12345&moTime=1012241002&mobile=13564900192&msg=hello&isReport=false

	对于每一个MO，都向客户端发送，无论对方接收服务是否在线。
	对于状态报告，当某一次发送由于接收方不在线导致发送失败，则暂停推送一个小时。在此期间到达的状态报告均不再重发。一小时后，再次尝试发送当前状态报告给对方。

12.	提交响应代码
代码	说明
0	提交成功
101	无此帐号或被禁用
102	密码错
103	提交过快（每秒钟提交手机号码个数超过流速限制）
104	系统忙（所有用户提交速率之和超过系统处理能力）
105	非法发送号（源号码错误）
106	敏感短信
107	消息长度错（纯英文短信，长度应为1~1215个字符，其他内容短信，长度应为1~585个字符）
108	手机号个数错（每个submit消息中包含的手机号码个数超过20个）
109	错误手机号
110	IP错
111	Msgid错
112	查询时间错
113	无查询标识
114	无发送权限
115	没有免费短信
116	黑名单企业
117	企业被暂停提交
119	虚拟号码超过流控


13.	状态报告列表
状态名	状态值(字符串)	说明
DELIVERED	DELIVRD	短消息转发成功
EXPIRED	EXPIRED	短消息超过有效期
DELETED	DELETED	短消息已经被删除
UNDELIVERABLE	UNDELIV	短消息是不可转发的
ACCEPTED	ACCEPTD	短消息已经被最终用户接收
UNKNOWN	UNKNOWN	未知短消息状态(系统生成)
REJECTED	REJECTD	短消息被拒绝
DTBLACK	DTBLACK	目的号码是黑名单号码
VMFLUCTL	VMFLUCTL	虚拟号码超流速(系统生成)
其他		网关内部状态

14.	短信内容编码：UTF-8
	提交的短信内容，需要编码为UTF-8的格式
	查询或是推送，短信回复编码为UTF-8的格式





1.	提交响应代码
代码	说明	提交方式
0	提交成功	HTTP、SMGP
101	无此帐号或被禁用	SMGP
102	密码错	HTTP、SMGP
103	提交过快（每秒钟提交手机号码个数超过流速限制）	HTTP、SMGP
104	系统忙（所有用户提交速率之和超过系统处理能力）	HTTP、SMGP
105	非法发送号（源号码错误）	HTTP、SMGP
106	敏感短信	HTTP、SMGP
107	消息长度错
对于HTTP方式：纯英文短信，长度应为1~1215个字符，其他内容短信，长度为1~585个字符
对于SMGP方式：msgFormat=0时，长度应为1~140个字符，其他格式长度应为1~70个字符	HTTP、SMGP
108	手机号个数错（每个submit消息中包含的手机号码个数超过20个）	HTTP、SMGP
109	错误手机号	HTTP、SMGP
110	IP错（登录IP不是绑定IP）	HTTP、SMGP
111	MsgId错	HTTP
112	查询时间错	HTTP
113	无查询标识	HTTP
114	无发送权限	HTTP、SMGP
115	没有免费短信	HTTP、SMGP
116	黑名单企业	HTTP、SMGP
117	企业被暂停提交	HTTP、SMGP
119	虚拟号码超过流控	HTTP、SMGP


2.	状态报告列表
状态名	状态值(字符串)	说明
DELIVERED	DELIVRD	短消息转发成功
EXPIRED	EXPIRED	短消息超过有效期
DELETED	DELETED	短消息已经被删除
UNDELIVERABLE	UNDELIV	短消息是不可转发的
ACCEPTED	ACCEPTD	短消息已经被最终用户接收
UNKNOWN	UNKNOWN	未知短消息状态(系统生成)
REJECTED	REJECTD	短消息被拒绝
DTBLACK	DTBLACK	目的号码是黑名单号码
VMFLUCTL	VMFLUCTL	虚拟号码超流速(系统生成)
其他		网关内部状态


