package org.emis.tayvs.jenkins

import scalaj.http.Http

object Run extends App {
  
  val jenkinsHost = "http://10.34.0.29:8080"
  
  def rootInfo() = {
    Http(jenkinsHost + "/api/json?pretty=true&token=528c9fbec6a89af103ece4062732cbe5")
//      .header("user", "vsiokh:528c9fbec6a89af103ece4062732cbe5")
      .auth("vsiokh", "vsiokh:528c9fbec6a89af103ece4062732cbe5")
//      .auth("vsiokh", "vsiokh:Vetal!@#$")
//      .postForm
      .asString
  }
  
  println(rootInfo())
  
}
