/*
 *  Copyright 2014 Leonid Dubinsky <dub@podval.org>.
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
 */

package org.podval.judaica.webapp

import org.podval.judaica.viewer.Works

import javax.ws.rs.{Produces, GET, PathParam, Path}
import javax.ws.rs.core.MediaType

import java.io.File


final class FontsResource {

  @GET
  @Path("{font}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  def font(@PathParam("font") name: String) = new File(new File(Works.directory, "fonts"), name)
}
