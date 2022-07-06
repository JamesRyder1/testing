package sample

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets, GoogleCredential}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.content.model.{AccountStatus, AccountstatusesListResponse}
import com.google.api.services.content.{ShoppingContent, ShoppingContentScopes}

import java.io.{FileInputStream, InputStreamReader}
import java.nio.file.Paths
import java.util
import javax.print.attribute.standard.Severity
import scala.jdk.CollectionConverters._
import scala.util.Using

object GetAccountStatus extends App {

  val httpTransport: NetHttpTransport =
    GoogleNetHttpTransport.newTrustedTransport()
  val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
  val scopes: util.Set[String] = ShoppingContentScopes.all()
  val initializer: Credential =
    authenticateWithOAuthClient() // Use authenticateWithServiceAccount() when the service account has been addedo to GMC

  val builder: ShoppingContent.Builder = new ShoppingContent.Builder(
    httpTransport,
    jsonFactory,
    initializer
  )

  val content: ShoppingContent = builder
    .build()

  val x: AccountstatusesListResponse =
    content.accountstatuses().list(BigInt(510935342).bigInteger).execute()
  x.getResources

  //

  val get = content
    .accountstatuses()
    .get(BigInt(510935342).bigInteger, BigInt(523721164).bigInteger)
  val accountStatus: AccountStatus = get.execute()
  println(accountStatus)
  accountStatus.getProducts.asScala.foreach { p =>
    p.getItemLevelIssues.asScala.foreach { itemLevelIssue =>
      println("------------------")
      println("Item level issues:")
      println(itemLevelIssue.getCode) // issue error as per GMC diagnostics page
      println(itemLevelIssue.getDetail) // description of what is wrong with the product listing
      println(itemLevelIssue.getAttributeName) // attribute of the impacted item (i.e. description, image, etc.)
      println(itemLevelIssue.getNumItems) // total number of items affected
      println(itemLevelIssue.getServability) // informs us whether products were approved or not
      println(itemLevelIssue.getDetail)
      println("severity: " + itemLevelIssue.get("severity"))
    }
  }
  accountStatus.getAccountLevelIssues.asScala.foreach { issue =>
    println("severity :")
    println(issue.getSeverity)
    println(issue.getTitle)
    println(issue.getDocumentation)
  }


  /*val get2 = content
    .productstatuses()
    .get(BigInt(510935342).bigInteger, BigInt(304510015-6).bigInteger)
  val accountStatus = get.execute()
  println(accountStatus)
  accountStatus.getProducts.asScala.foreach { p =>
    p.getItemLevelIssues.asScala.foreach { itemLevelIssue =>*/


  private def authenticateWithServiceAccount(): Credential = {
    val serviceSecret =
      Paths.get("james-and-jari-gmc-status-test-6f41118e0733.json").toFile
    Using.resource(new FileInputStream(serviceSecret)) { inputStream =>
      GoogleCredential
        .fromStream(inputStream, httpTransport, jsonFactory)
        .createScoped(scopes)
    }
  }

  /** For local testing before we have the service account added as user to GMC. */
  private def authenticateWithOAuthClient(): Credential = {
    val fileName =
      "client_secret_978230881376-ckrf13j8ti3frtn4mn1ptmd9a1po5vih.apps.googleusercontent.com.json"

    val clientSecretFile =
      Paths.get(fileName).toFile
    Using.resource(new FileInputStream(clientSecretFile)) { inputStream =>
      val clientSecrets = GoogleClientSecrets.load(
        jsonFactory,
        new InputStreamReader(inputStream)
      )
      val flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport,
        jsonFactory,
        clientSecrets,
        scopes
      ).build()
      val receiver = new LocalServerReceiver.Builder()
        .setHost("localhost")
        .setPort(9999)
        .build
      val credential =
        new AuthorizationCodeInstalledApp(flow, receiver).authorize(null)
      println(s"Received credentials for user")
      return credential
    }
  }
}
