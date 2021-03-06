package nl.cerios.reactive.chocolate.factory.verticle

import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.AbstractVerticle
import nl.cerios.reactive.chocolate.factory.delayAndNotifyConsumption
import nl.cerios.reactive.chocolate.factory.model.ColorNut
import nl.cerios.reactive.chocolate.factory.model.MnM
import org.slf4j.LoggerFactory

class LetterStamper : AbstractVerticle() {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun start() {
    val processingMillis = config().getLong("${javaClass.name}.processingMillis")

    vertx.eventBus()
        .consumer<JsonObject>("colorNut.produced")
        .toObservable()
        .delayAndNotifyConsumption(vertx, 2_000, processingMillis)
        .map { message -> ColorNut.fromJson(message.body()) }
        .map { colorNut -> MnM(colorNut).toJson() }
        .subscribe(
            { mnmJson -> vertx.eventBus().publish("mnm.produced", mnmJson) },
            { throwable -> log.error("Error stamping colorNuts.", throwable) })
  }
}
