package org.scardiecat.microservice.marshalling

import java.text.SimpleDateFormat
import java.util.{UUID, Date}

import spray.http.{ContentTypes, HttpEntity, StatusCodes, HttpResponse}
import spray.httpx.marshalling.{ToResponseMarshallingContext, ToResponseMarshaller, MetaMarshallers}
import spray.httpx.unmarshalling._
import spray.json._
import spray.routing.directives.MarshallingDirectives

import scala.util.Try

trait CommonProtocolMarshallers extends MarshallingDirectives with MetaMarshallers {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  implicit object DateFSOD extends FromStringOptionDeserializer[Date] {

    override def apply(v: Option[String]): Deserialized[Date] = {
      v.map { s ⇒ Try { Right(dateFormat.parse(s)) }.getOrElse(Left(MalformedContent("Invalid date " + s))) } getOrElse Left(ContentExpected)
    }
  }

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(obj: UUID) = JsString(obj.toString)
    def read(json: JsValue) = (json: @unchecked) match {
      case JsString(x) ⇒ UUID.fromString(x)
    }
  }

  implicit object UnitToResponseMarshaller extends ToResponseMarshaller[Unit] {
    override def apply(value: Unit, ctx: ToResponseMarshallingContext): Unit =
      ctx.marshalTo(HttpResponse(StatusCodes.OK, entity = HttpEntity(contentType = ContentTypes.`application/json`, string = "{}")))
  }

  implicit object DateFormat extends JsonFormat[Date] {
    override def read(json: JsValue): Date = (json: @unchecked) match {
      case JsString(v) ⇒ dateFormat.parse(v)
    }

    override def write(obj: Date): JsValue = JsString(dateFormat.format(obj))
  }

  implicit  object ResponseFormat extends  {

  }
}
