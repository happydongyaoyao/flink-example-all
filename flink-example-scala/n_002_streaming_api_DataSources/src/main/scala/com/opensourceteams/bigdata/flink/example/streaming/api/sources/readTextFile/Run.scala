package com.opensourceteams.bigdata.flink.example.streaming.api.sources.readTextFile

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

case class Animal(id:String,name:String)
object Run {

  def main(args: Array[String]): Unit = {

    val env:StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val dataStream:DataStream[String] = env.readTextFile("D:\\workspaces\\bigdata\\flink\\flink-example-all/data/text/text.txt")


    dataStream.print()
    env.execute("test")
  }
}
