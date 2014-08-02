package au.com.onegeek.respite.config

import com.escalatesoft.subcut.inject._
import reactivemongo.api.DefaultDB
import _root_.akka.actor.ActorSystem

/**
 * Created by mfellows on 24/04/2014.
 */
object ProductionConfigurationModule extends NewBindingModule(module => {
  import module._

  bind[DefaultDB] toProvider {
    ApiDatasource.getConnection
  }

  bind[ActorSystem] toProvider {
    ActorSystem()
  }
})

object TestConfigurationModule extends NewBindingModule(module => {
  import module._

  bind[DefaultDB] toProvider {
    ApiDatasource.getConnection
  }

  bind[ActorSystem] toProvider {
    ActorSystem()
  }


})