package org.opentorah.texts.tanach

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class TanachTest extends AnyFlatSpec with Matchers {

  "Tanach" should "load" in {
    Chumash.Genesis.chapters.length(17) shouldBe 27
    Parsha.Vayikra.aliyot.spans(2).span.from.verse shouldBe 10
  }
}
