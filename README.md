C-style assert in Scala
=======================
This is a Scala 2.9 compiler plugin(<bold>cassert.jar</bold>) that supports C-style assert.
for example, the following assert and require call in Main.scala:
<pre>
object Main extends App 
{
    val v1 = 1
    val v2 = 2
    val v3 = "this string should have at leat 20 chars"
	        
    assert(v1 > v2)
    require(v3.length()<20)
}			    
</pre>

the assert call would produce the following stacktrace, which is hard to read, particularly, if the application 
is running at production site.


<pre>Exception in thread "main" java.lang.AssertionError: assertion failed
	at scala.Predef$.assert(Predef.scala:89)
	at com.sigma.jvmassert.Main2$delayedInit$body.apply(Main2.scala:9)
	at scala.Function0$class.apply$mcV$sp(Function0.scala:34)
	at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:12)
	at scala.App$$anonfun$main$1.apply(App.scala:60)
	at scala.App$$anonfun$main$1.apply(App.scala:60)
	at scala.collection.LinearSeqOptimized$class.foreach(LinearSeqOptimized.scala:59)
	at scala.collection.immutable.List.foreach(List.scala:45)
	at scala.collection.generic.TraversableForwarder$class.foreach(TraversableForwarder.scala:30)
	at scala.App$class.main(App.scala:60)
	at com.sigma.jvmassert.Main$.main(Main2.scala:3)
	at com.sigma.jvmassert.Main.main(Main2.scala)
</pre>

The same is true for the require method call.

The cassert plugin would rewrite the assert or require method call and produce the following stacktrace:
NOTE the error message has <bold>file Main.scala, line 7:v1 > v2</bold>


Exception in thread "main" java.lang.AssertionError: assertion failed: <bold>file Main.scala, line 7:v1 > v2)</bold>
<pre>	at scala.Predef$.assert(Predef.scala:103)
	at Main$delayedInit$body.apply(Main.scala:7)
	at scala.Function0$class.apply$mcV$sp(Function0.scala:34)
	at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:12)
	at scala.App$$anonfun$main$1.apply(App.scala:60)
	at scala.App$$anonfun$main$1.apply(App.scala:60)
	at scala.collection.LinearSeqOptimized$class.foreach(LinearSeqOptimized.scala:59)
	at scala.collection.immutable.List.foreach(List.scala:45)
	at scala.collection.generic.TraversableForwarder$class.foreach(TraversableForwarder.scala:30)
	at scala.App$class.main(App.scala:60)
	at Main$.main(Main.scala:1)
	at Main.main(Main.scala)
</pre>
	
Stack trace of the require call with the cassert plugin:

Exception in thread "main" java.lang.IllegalArgumentException: requirement failed: <bold>file Main.scala, line 8:v3.length()<20)</bold>
<pre>	at scala.Predef$.require(Predef.scala:157)
	at Main$delayedInit$body.apply(Main.scala:8)
	at scala.Function0$class.apply$mcV$sp(Function0.scala:34)
	at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:12)
	at scala.App$$anonfun$main$1.apply(App.scala:60)
	at scala.App$$anonfun$main$1.apply(App.scala:60)
	at scala.collection.LinearSeqOptimized$class.foreach(LinearSeqOptimized.scala:59)
	at scala.collection.immutable.List.foreach(List.scala:45)
	at scala.collection.generic.TraversableForwarder$class.foreach(TraversableForwarder.scala:30)
	at scala.App$class.main(App.scala:60)
	at Main$.main(Main.scala:1)
	at Main.main(Main.scala)	
</pre>
		