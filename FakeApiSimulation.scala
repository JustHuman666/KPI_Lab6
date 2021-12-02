package GoRest
import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object RandomGenerator {
    def randomString(length: Int): String = {
        val POSSIBLECHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        val newString = new StringBuilder
        val rand = new scala.util.Random
        while (newString.length < length) { 
            val index = (rand.nextFloat() * POSSIBLECHAR.length).asInstanceOf[Int]
            newString.append(POSSIBLECHAR.charAt(index))
        }
        val finalString = newString.toString
        finalString
    }

    def randomRequest() : String = 
        """{"name":"""".stripMargin + RandomGenerator.randomString(10) + """",
        |"job":"""".stripMargin + RandomGenerator.randomString(15) + """"} 
        |""".stripMargin
                            
}

class RandomUserSimulation extends Simulation{
    val baseUrl = http.baseUrl("https://reqres.in/api/")

    val postUser = scenario("Create User (Post)")
    .exec(sessionPost => {
      val sessionPostCreate = sessionPost.set("postRequest", RandomGenerator.randomRequest())
      sessionPostCreate
    })
    .exec(
      http("Create User (Post)")
        .post("/users")
        .body(StringBody("${postRequest}")).asJson
    )

    val getUsers = scenario("Get the list of users")
    .exec(
        http("Get the list of users")
        .get("/users?page=2")
    )

    val getUser = scenario("Get User by id")
    .exec(
        http("Get the list of users")
        .get("/users?page=2")
        .check(jsonPath("$.data[0].id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(
      http("Get User by id")
        .get("/users/${userId}")
    )

    val putUser = scenario("Update user info (Put)")
    .exec(sessionPost => {
      val sessionPostCreate = sessionPost.set("postRequest", RandomGenerator.randomRequest())
      sessionPostCreate
    })
    .exec(
        http("Create User (Post)")
        .post("/users")
        .body(StringBody("${postRequest}")).asJson
        .check(jsonPath("$.id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putRequest", RandomGenerator.randomRequest())
      sessionPutUpdate
    })
    .exec(
      http("Update user info (Put)")
        .put("/users/${userId}")
        .body(StringBody("${putRequest}")).asJson
    )

    val deleteUser = scenario("Delete user")
    .exec(sessionPost => {
      val sessionPostCreate = sessionPost.set("postRequest", RandomGenerator.randomRequest())
      sessionPostCreate
    })
    .exec(
        http("Create User (Post)")
        .post("/users")
        .body(StringBody("${postRequest}")).asJson
        .check(jsonPath("$.id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(
      http("Delete user")
      .delete("/users/${userId}")
    )

    setUp(
    postUser.inject(rampUsers(10).during(10.seconds)).protocols(baseUrl),
    getUsers.inject(rampUsers(10).during(10.seconds)).protocols(baseUrl),
    getUser.inject(rampUsers(10).during(10.seconds)).protocols(baseUrl),
    putUser.inject(rampUsers(10).during(10.seconds)).protocols(baseUrl),
    deleteUser.inject(rampUsers(10).during(10.seconds)).protocols(baseUrl))
}