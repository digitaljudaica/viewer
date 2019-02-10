/*
 *  Copyright 2011 Leonid Dubinsky <dub@podval.org>.
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

package org.podval.judaica.importers

import scala.xml.{Elem, XML}
import scala.xml.factory.XMLLoader

import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl


object TagSoupXmlLoader {

    private val factory = new SAXFactoryImpl()


    def get(): XMLLoader[Elem] = {
        XML.withSAXParser(factory.newSAXParser())
    }
}