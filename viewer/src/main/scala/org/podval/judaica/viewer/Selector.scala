/*
 * Copyright 2010 dub.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package org.podval.judaica.viewer

import scala.xml.Node


final class Selector(val what: String, val name: String) {

    def toName: String =
        what + "-" + name


    def toNameSpan: Node =
        <span class={what +"-name"}>{name}</span>
}


object Selector {

    def maybeFromXml(xml: Node): Option[Selector] =
        if (!isDiv(xml)) None else Some(fromXml(xml))


    def fromXml(xml: Node): Selector = {
        if (!isDiv(xml)) throw new IllegalArgumentException("Not a div!")

        new Selector(getType(xml), getName(xml))
    }


    def isDivType(what: String)(node: Node) = isDiv(node) && (getType(node) == what)


    private def getType(node: Node) = (node \ "@type").text


    def getName(node: Node) = (node \ "@n").text


    private def isDiv(node: Node) = (node.label == "div")


    def toName(selectors: Seq[Selector]): String = selectors.map(_.toName).mkString("-")
}
