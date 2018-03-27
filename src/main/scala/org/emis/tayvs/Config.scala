package org.emis.tayvs

import com.typesafe.config.ConfigFactory

object Config extends App {
  
  println(ConfigFactory.load().getConfig("app").getString("myHOmeDir"))

}
