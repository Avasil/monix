/*
 * Copyright (c) 2014-2018 by The Monix Project Developers.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.execution.internal

import monix.execution.UncaughtExceptionReporter

import scala.util.{Failure, Success, Try}

/** Internal API — some utilities for working with cats-effect
  * callbacks.
  */
private[monix] object AttemptCallback {

  /** Reusable reference for ticks. */
  final val tick: Either[Throwable, Unit] = Right(())

  /** Reusable runnable that triggers a tick. */
  final class RunnableTick(cb: Either[Throwable, Unit] => Unit)
    extends Runnable {

    def run(): Unit =
      cb(tick)
  }

  /** Creates a callback that does nothing on success, but that
    * reports errors on failure, similar with `Callback.empty`.
    */
  def empty(implicit r: UncaughtExceptionReporter): Either[Throwable, Unit] => Unit = {
    case Left(e) => r.reportFailure(e)
    case _ => ()
  }

  /** Converts an attempt callback into one that uses `Try`
    * (to be used with `Future.onComplete`).
    */
  def toTry[A](cb: Either[Throwable, A] => Unit): (Try[A] => Unit) = {
    case Success(a) => cb(Right(a))
    case Failure(e) => cb(Left(e))
  }
}
