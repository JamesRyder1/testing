package sample

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{
  GoogleAuthorizationCodeFlow,
  GoogleClientSecrets,
  GoogleCredential
}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.content.model.AccountstatusesListResponse
import com.google.api.services.content.{ShoppingContent, ShoppingContentScopes}

import java.io.{FileInputStream, InputStreamReader}
import java.nio.file.Paths
import java.util
import scala.jdk.CollectionConverters._
import scala.util.Using

object GetAccountStatusList extends App {

  val httpTransport: NetHttpTransport =
    GoogleNetHttpTransport.newTrustedTransport()
  val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
  val scopes: util.Set[String] = ShoppingContentScopes.all()
  val initializer: Credential =
    authenticateWithOAuthClient() // Use authenticateWithServiceAccount() when the service account has been added to GMC

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

  val get = content
    .accountstatuses()
    .get(BigInt(510935342).bigInteger, BigInt(142650914).bigInteger)
  val accountStatus = get.execute()
  println(accountStatus)
  accountStatus.getProducts.asScala.foreach { p =>
    p.getItemLevelIssues.asScala.foreach { itemLevelIssues =>
      println("---------------")
      println("Item level issues:")
      println(itemLevelIssues.getCode) // keep
      println(itemLevelIssues.getDescription) // keep
      // println(itemLevelIssues.getDetail) // keep
      println(itemLevelIssues.getAttributeName) // keep
      println(itemLevelIssues.getNumItems) // keep
      // println(itemLevelIssues.getDocumentation) // keep - documentation
      // println(itemLevelIssues.getResolution) - not valuable
      ///println(itemLevelIssues.getServability) - not valuable
      // println(itemLevelIssues.getClassInfo) - not valuable
      // println(itemLevelIssues.getClass) - not valuable
      // println(itemLevelIssues.getFactory) - not valuable
      // println(itemLevelIssues.getUnknownKeys) - not valuable
    }
  }

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
