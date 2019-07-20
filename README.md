## Eorm idea plugin
<p>Used for <a target="_blank" href="https://github.com/deng-hb/eorm-spring">Eorm-spring</a></p>
<p>Eorm based spring-jdbc JdbcTemplate & NamedParameterJdbcTemplate a every every good ORM framework.</p>

### Changelog
<h3>v1.0.3</h3>
<ul>
<li>add smart tip table name </li>
<li>add `(`,`)`,`#if`,`#elseIf`,`#else`,`#end`.. keywords highlight</li>
</ul>

<h3>v1.0.2</h3>
<p>SQL Smart Tip</p>
<h5>step 1:</h5>
<p>Create name <code>eorm.config</code> file to Project root path</p>
<h5>step 2:</h5>
<p>Write your MySQL connect info</p>
<p>
<pre><code style="color:#203DBF">
host=localhost
port=3306
database=test
username=root
password=
</code>
</pre>
</p>
<h5>done</h5>
<img src="https://raw.githubusercontent.com/deng-hb/eorm-idea-plugin/master/eorm-smart-tip.gif" />

<h3>v1.0.1</h3>
<p><a target="_blank" href="https://github.com/deng-hb/multi-line">Multi-line</a> SQL Highlight</p>
<p>
<pre>
<code style="color:#203DBF">
<span style="color:#333">String sql = </span>""<span style="color:#aaa">/*{</span>
<span style="color:#008000">select</span> * <span style="color:#008000">from</span> tb_user
<span style="color:#aaa">}*/</span><span style="color:#333">;</span>
</code>
</pre>
</p>