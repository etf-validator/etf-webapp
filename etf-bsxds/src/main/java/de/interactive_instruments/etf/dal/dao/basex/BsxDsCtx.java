/**
 * Copyright 2010-2022 interactive instruments GmbH
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
package de.interactive_instruments.etf.dal.dao.basex;

import java.util.Collection;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.basex.core.Context;
import org.slf4j.Logger;

import de.interactive_instruments.IFile;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * Basex data storage context
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
interface BsxDsCtx {

    IFile getStoreDir();

    Context getBsxCtx();

    Unmarshaller createUnmarshaller() throws JAXBException;

    Marshaller createMarshaller() throws JAXBException;

    Logger getLogger();

    Dto getFromCache(final EID eid);

    Object createProxy(final EID eid, final Class<? extends Dto> type) throws ObjectWithIdNotFoundException, StorageException;

    void delete(final Collection<? extends Dto> dtos);

    void deleteFiles(Collection<? extends Dto> dtos);
}
