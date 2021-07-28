package de.lolhens.serverdesktop

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactEventFromInput, ScalaComponent}

import scala.util.{Failure, Success}

object MainComponent {
  case class Props()

  case class State(
                    apps: Seq[App],
                    filter: String
                  )

  object State {
    val empty: State = State(Seq.empty, "")
  }

  private val itemsPerRow = 4

  class Backend($: BackendScope[Props, State]) {
    Backend.apps().completeWith {
      case Success(apps) => $.modState(_.copy(apps = apps))
      case Failure(exception) =>
        exception.printStackTrace()
        throw exception
    }.runNow()

    def render: VdomElement = {
      val state = $.state.runNow()

      <.div(
        ^.cls := "container my-4 d-flex flex-column",
        <.h1(
          ^.id := "settings",
          ^.position := "relative",
          <.i(
            ^.cls := "bi bi-gear",
            ^.position := "absolute",
            ^.right := "0",
            ^.top := "0.8rem",
            ^.cursor := "pointer",
            ^.onClick --> Callback {
              println("Settings")
            }
          )
        ),
        <.input(
          ^.id := "search",
          ^.cls := "align-self-center form-control form-control-lg mb-4",
          ^.maxWidth := "20em",
          ^.boxShadow := "2px 2px 8px rgb(0, 0, 0, 20%)",
          ^.tpe := "text",
          ^.placeholder := "Search...",
          ^.onChange ==> { e: ReactEventFromInput =>
            val value = e.target.value
            $.modState(_.copy(filter = value))
          }
        ),
        <.div(
          ^.id := "apps",
          ^.cls := "flex-fill d-flex flex-row flex-wrap",
          state.apps.filter(_.title.toLowerCase.contains(state.filter.toLowerCase)).map { app =>
            <.div(
              ^.key := app.id.string,
              ^.cls := "p-2",
              ^.width := s"${100 / itemsPerRow}%",
              AppComponent.Component(AppComponent.Props(app))
            )
          }.toVdomArray
        )
      )
    }
  }

  val Component =
    ScalaComponent.builder[Props]
      .initialState(State.empty)
      .backend(new Backend(_))
      .render(_.backend.render)
      .build
}
