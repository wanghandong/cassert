package com.sigma.cassert

import scala.tools.nsc.symtab.Flags._
import scala.tools.nsc.Global
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.transform._
import collection.mutable.{ LinkedHashMap, Stack, HashMap }
import scala.tools.nsc.util.OffsetPosition

/**
 * Scala compiler plugin which support C-style assert.
 */
class CAssert(val global: Global) extends Plugin {

  val name = "cassert"
  val description = "support C-style assert in Scala"
  val components: List[PluginComponent] = List(CAssertInjector)

  object CAssertInjector extends PluginComponent with Transform with TypingTransformers {

    import global._

    val global = CAssert.this.global
    val phaseName = "cassert"

    val runsAfter = List("parser");
    override val runsBefore = List[String]("namer")

    class ModuleInfo {
      var using_asserts = false
      var module_exists = false
    }
    
    protected def newTransformer(unit: CompilationUnit): Transformer = new Transformer {
      import scala.tools.nsc.util.Position._
      
      def printPos(node:Tree){
        if (node.pos.isDefined) {
          println(node.pos.line + ":" + node.pos.column + " " + node.toString())
        } else {
          println(node.pos + " " + node.toString());
        }        
      }
      
      /**
       * printing a syntax tree
       */
      def traverseTreeNode(node: Tree, indent: Int) {
        for (i <- 1 to indent) {
          print("|    ")
        }
        print(node.getClass().getName()+" ")
        node match{
          case Select(qualifier, name) =>
            print("name="+name+" ")            
          case _ => 
        }
        printPos(node)
        for (c <- node.children) {
          traverseTreeNode(c, indent + 1)
        }
      }
      
      def eq(name:Name, s:String) = name.toString()==s
      
      
      
      /**
       * retrieve source code text given its range      
       */
      private def getText(content:Array[Char], startLine: Int, startCol: Int,
                  endLine:Int, endCol:Int):String =
      {
        val buf = new StringBuilder()
        
        var line = 1
        var col = 0
        
        def inRange():Boolean = {
          if(line < startLine)
            return false
          if(line > endLine)
            return false
          if(line == startLine && col < startCol)
            return false
          if(line == endLine && col > endCol)
            return false
          
          true
        }
        
        for(c <- content){
          c match {
            case '\n' => line += 1; col = 0
            case _ => col += 1
          }
          if(inRange()){ 
            buf.append(c)
            if(line==endLine && col==endCol){
              //
            }
          }
        }
        
        return buf.toString()
      }

      if(true){
        println(" =========== dump source file ========= ")
        unit.source.content.foreach(print)
        
        println("\n ========== tree structure before modification ========")
        traverseTreeNode(unit.body, 0)
        println("unit.source="+unit.source.getClass().getName())
      }

      /**
       * figure out (rough) range of source code of the given syntax tree
       */
      class NodeRange(val tree: Tree) {
        var startLine = -1
        var startCol = -1
        var endLine = -1
        var endCol = -1

        calculate(tree)

        
        private def setStart(pos:Position){
              startLine = pos.line
              startCol = pos.column          
        }
        private def setEnd(pos:Position){
              endLine = pos.line
              endCol = pos.column          
        }
        private def setEnd(line:Int, col:Int){
              endLine = line
              endCol = col          
        }
        private def comparePos(line1:Int,col1:Int, line:Int, col:Int):Int = {
          if(line1 < line){
            return -1
          }else if(line1 > line){
            return +1
          }else{
            if(col1 < col){
              return -1
            }else if(col1 > col){
              return +1
            }else{
              return 0
            }
          }
        }
        
        private def calculate(node:Tree){
          
          def extendColumn(node:Tree):Int =
            node match{
            case Ident(name) => name.toString().length()
            case Literal(_) => node.toString().length()
            case Select(_,name) if(!name.toString().contains("$")) => name.toString().length() 
            case _ => 0
            }
          
          
          if (node.pos.isDefined) {
            if(startLine < 0){
              setStart(node.pos)
            }else{
              if(comparePos(node.pos.line, node.pos.column, startLine, startCol)<0){
                setStart(node.pos)
              }
            }
            if(endLine < 0){
                setEnd(node.pos)              
            }
            {
              val ext = extendColumn(node)
              if(ext>0){
                val line = node.pos.line
                val col = node.pos.column+ext
                println("extend column of "+(node.pos.line,node.pos.column)+" node "+node)
                if (comparePos(line, col, endLine, endCol) > 0) 
                    setEnd(line, col)
              } else if (comparePos(node.pos.line,node.pos.column, endLine, endCol) > 0) {
                 setEnd(node.pos)
              }
              
            }
            node.children.foreach(calculate)
          }
        }
      }
      
      
      override def transform(node: Tree): Tree = {
        node match{
          case Apply(f @ Ident(name), a1::Nil) if eq(name,"assert") || eq(name,"require") =>
               
               val nodeRange = new NodeRange(a1)
               val expr = "file "+unit.source.file.name +", line "+nodeRange.startLine +":"+   
                    getText(unit.source.content, nodeRange.startLine, nodeRange.startCol,
                            nodeRange.endLine, nodeRange.endCol)
                    
               println("rewrite "+node.getClass().getName() + " " + node.pos.line + ":" + node.pos.column + 
                       " " + node.toString()+" expr="+expr)
               println("range="+(nodeRange.startLine,nodeRange.startCol,nodeRange.endLine,nodeRange.endCol))        
               val a2 = Literal(Constant(expr))
               
               
               treeCopy.Apply(node, f, a1 :: a2 :: Nil)
          case _ => super.transform(node) 
        }        
      }

    }
  }

}