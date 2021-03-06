package com.github.mideo.media.m3u8.io

import java.util

import com.github.mideo.M3U8ParserSuite

import scala.collection.JavaConverters._
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Future


class HttpTest extends M3U8ParserSuite with BeforeAndAfterAll {
  val headers: util.Map[String, util.List[String]] = Map("header1" -> util.Arrays.asList("value1")).asJava
  val wireMockServer = new WireMockServer()

  override protected def beforeAll(): Unit = {
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.shutdownServer()
  }

  test("write with Post") {
    //Given
    val request = Http.Request("POST", wireMockServer.url("/test"), Map("header2" -> "value2"), Some("payload".getBytes()), 3000, 2000)
    wireMockServer.stubFor(post(urlEqualTo("/test"))
      .willReturn(aResponse()
        .withHeader("header1", "value1")
        .withBody("abcs")))

    //When
    val response: Future[Http.Response] = Http.write(request)

    //Then
    response map {
      it: Http.Response =>
        wireMockServer.verify(postRequestedFor(urlEqualTo("/test"))
          .withHeader("header2", equalTo("value2"))
          .withRequestBody(equalTo("payload")))
        it.Headers.keySet should contain("header1")
        it.Headers("header1") should be("value1")
        new String(it.content) should be("abcs")

    }

  }

  test("write with  Get") {
    //Given
    wireMockServer.stubFor(get(urlEqualTo("/test"))
      .willReturn(aResponse()
        .withHeader("header1", "value1")
        .withBody("abcs")))

    //When
    val response: Future[Http.Response] = Http.write(Http.Request("GET", wireMockServer.url("/test")))

    //Then
    response map {
      it: Http.Response =>
        wireMockServer.verify(getRequestedFor(urlEqualTo("/test")))
        it.Headers.keySet should contain("header1")
        it.Headers("header1") should be("value1")
        new String(it.content) should be("abcs")
    }

  }
}
