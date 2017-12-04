/**
 * Copyright (C) 2017 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.download

import java.net.URI

import nl.knaw.dans.easy.download.components.{ AuthInfoComponent, BagStoreComponent, HttpWorkerComponent }

/**
 * Initializes and wires together the components of this application.
 */
trait ApplicationWiring extends HttpWorkerComponent
  with AuthInfoComponent
  with BagStoreComponent {

  /**
   * the application configuration
   */
  val configuration: Configuration

  override val bagStore: BagStore = new BagStore {
    override val baseUri: URI = new URI(configuration.properties.getString("bag-store.url"))
  }
  override val authInfo: AuthInfo = new AuthInfo {
    override val baseUri: URI = new URI(configuration.properties.getString("auth-info.url"))
  }
}