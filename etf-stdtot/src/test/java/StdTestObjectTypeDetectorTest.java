/*
 * Copyright 2010-2017 interactive instruments GmbH
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

import de.interactive_instruments.Credentials;
import de.interactive_instruments.IFile;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.etf.dal.dto.capabilities.ResourceDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectDto;
import de.interactive_instruments.etf.detector.DetectedTestObjectType;
import de.interactive_instruments.etf.detector.IncompatibleTestObjectTypeException;
import de.interactive_instruments.etf.detector.TestObjectTypeDetectorManager;
import de.interactive_instruments.etf.detector.TestObjectTypeNotDetected;
import de.interactive_instruments.etf.model.capabilities.LocalResource;
import de.interactive_instruments.etf.model.capabilities.Resource;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class StdTestObjectTypeDetectorTest {

    private static Resource create(final String url) throws URISyntaxException {
        return Resource.create("test", new URI(url), null);
    }

	@Test
	public void test11_Wfs20() throws URISyntaxException, IOException, TestObjectTypeNotDetected {
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
		    create("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
                + "?request=GetCapabilities&service=wfs"));
		assertNotNull(detectedType);
		assertEquals("9b6ef734-981e-4d60-aa81-d6730a1c6389", detectedType.getId().toString());
		assertEquals("db12feeb-0086-4006-bc74-28f4fdef0171", detectedType.getParent().getId().toString());

		final TestObjectDto testObject = new TestObjectDto();
		testObject.addResource(new ResourceDto("serviceEndpoint", "http://none"));
		detectedType.enrichAndNormalize(testObject);

		assertEquals("SimpleDemo WFS", testObject.getLabel());
		assertEquals("SimpleDemo WFS by XtraServer", testObject.getDescription());
		assertEquals("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
				+ "?ACCEPTVERSIONS=2.0.0&request=GetCapabilities&service=WFS&VERSION=2.0.0", testObject.getResourceByName("serviceEndpoint").toString());
	}

	@Test
	public void test11_Wfs20_secured() throws URISyntaxException, IOException, TestObjectTypeNotDetected {
		// Important: only the http endpoint is secured with a password
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
				Resource.create("test",
						new URI("http://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-secured/wfs"
								+ "?request=GetCapabilities&service=wfs"), new Credentials("etf", "etf")));
		assertNotNull(detectedType);
		assertEquals("9b6ef734-981e-4d60-aa81-d6730a1c6389", detectedType.getId().toString());
		assertEquals("db12feeb-0086-4006-bc74-28f4fdef0171", detectedType.getParent().getId().toString());

		final TestObjectDto testObject = new TestObjectDto();
		testObject.addResource(new ResourceDto("serviceEndpoint", "http://none"));
		detectedType.enrichAndNormalize(testObject);

		assertEquals("SimpleDemo WFS", testObject.getLabel());
		assertEquals("SimpleDemo WFS by XtraServer", testObject.getDescription());
		assertEquals("http://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-secured/wfs"
				+ "?ACCEPTVERSIONS=2.0.0&request=GetCapabilities&service=WFS&VERSION=2.0.0", testObject.getResourceByName("serviceEndpoint").toString());
	}

	@Test
	public void test12_Wfs11() throws URISyntaxException, IOException, TestObjectTypeNotDetected,
			ObjectWithIdNotFoundException, IncompatibleTestObjectTypeException {
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
				create("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
								+ "?request=GetCapabilities&service=wfs"),
				TestObjectTypeDetectorManager.getTypes("bc6384f3-2652-4c7b-bc45-20cec488ecd0").keySet());
		assertNotNull(detectedType);
		assertEquals("bc6384f3-2652-4c7b-bc45-20cec488ecd0", detectedType.getId().toString());
		assertEquals("db12feeb-0086-4006-bc74-28f4fdef0171", detectedType.getParent().getId().toString());

		final TestObjectDto testObject = new TestObjectDto();
		testObject.addResource(new ResourceDto("serviceEndpoint", "http://none"));
		detectedType.enrichAndNormalize(testObject);

		assertEquals("SimpleDemo WFS", testObject.getLabel());
		assertEquals("SimpleDemo WFS by XtraServer", testObject.getDescription());
		assertEquals("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
				+ "?request=GetCapabilities&service=WFS&VERSION=1.1.0", testObject.getResourceByName("serviceEndpoint").toString());
	}

	@Test
	public void test13_Wfs20SelectHighest() throws URISyntaxException, IOException, TestObjectTypeNotDetected,
			ObjectWithIdNotFoundException, IncompatibleTestObjectTypeException {
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
            create("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
								+ "?request=GetCapabilities&service=wfs"),
				TestObjectTypeDetectorManager.getTypes("db12feeb-0086-4006-bc74-28f4fdef0171").keySet());
		assertNotNull(detectedType);
		assertEquals("9b6ef734-981e-4d60-aa81-d6730a1c6389", detectedType.getId().toString());
		assertEquals("db12feeb-0086-4006-bc74-28f4fdef0171", detectedType.getParent().getId().toString());

		final TestObjectDto testObject = new TestObjectDto();
		testObject.addResource(new ResourceDto("serviceEndpoint", "http://none"));
		detectedType.enrichAndNormalize(testObject);

		assertEquals("SimpleDemo WFS", testObject.getLabel());
		assertEquals("SimpleDemo WFS by XtraServer", testObject.getDescription());
		assertEquals("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
				+ "?ACCEPTVERSIONS=2.0.0&request=GetCapabilities&service=WFS&VERSION=2.0.0", testObject.getResourceByName("serviceEndpoint").toString());
	}

	@Test
	public void test14_incompleteWfs20Url() throws URISyntaxException, IOException, TestObjectTypeNotDetected,
			ObjectWithIdNotFoundException, IncompatibleTestObjectTypeException {
		// request with version=2.0.0 parameter instead of acceptversions
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
            create("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
								+ "?request=GetCapabilities&service=wfs&version=2.0.0"));
		assertNotNull(detectedType);
		assertEquals("9b6ef734-981e-4d60-aa81-d6730a1c6389", detectedType.getId().toString());
		assertEquals("db12feeb-0086-4006-bc74-28f4fdef0171", detectedType.getParent().getId().toString());

		final TestObjectDto testObject = new TestObjectDto();
		testObject.addResource(new ResourceDto("serviceEndpoint", "http://none"));
		detectedType.enrichAndNormalize(testObject);

		assertEquals("SimpleDemo WFS", testObject.getLabel());
		assertEquals("SimpleDemo WFS by XtraServer", testObject.getDescription());
		assertEquals("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
				+ "?ACCEPTVERSIONS=2.0.0&request=GetCapabilities&service=WFS&version=2.0.0", testObject.getResourceByName("serviceEndpoint").toString());
	}

	@Test
	public void test15_incompatibleTypes() throws URISyntaxException, IOException, TestObjectTypeNotDetected,
			ObjectWithIdNotFoundException, IncompatibleTestObjectTypeException {
		// Expecting WMS 1.1.0 but provide WFS 2.0.0 URL
		try {
			final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
                create("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
									+ "?request=GetCapabilities&service=wfs&version=2.0.0"),
					// Expect WMS
					TestObjectTypeDetectorManager.getTypes("d1836a8d-9909-4899-a0bc-67f512f5f5ac").keySet());
		}catch (IncompatibleTestObjectTypeException e) {
			assertEquals("9b6ef734-981e-4d60-aa81-d6730a1c6389",
					e.getDetectedTestObjectType().getId().getId());
			return;
		}
		fail("Exception expected");
	}

	@Test
	public void test16_incompatibleTypes() throws URISyntaxException, IOException, TestObjectTypeNotDetected,
			ObjectWithIdNotFoundException, IncompatibleTestObjectTypeException {
		try {
			// Expecting WMS 1.3 but provide WFS 2.0.0 URL
			final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
                create("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
									+ "?request=GetCapabilities&service=wfs&version=2.0.0"),
					// Expect WMS 1.3
					TestObjectTypeDetectorManager.getTypes("9981e87e-d642-43b3-ad5f-e77469075e74").keySet());
		}catch (final IncompatibleTestObjectTypeException e) {
			assertEquals("9b6ef734-981e-4d60-aa81-d6730a1c6389",
					e.getDetectedTestObjectType().getId().getId());
			return;
		}
		fail("Exception expected");
	}

	private static void testUnknown() throws URISyntaxException, TestObjectTypeNotDetected, IOException {
        TestObjectTypeDetectorManager.detect(create("https://www.interactive-instruments.de"));
    }

	@Test
	public void test17_unknown()  {
        assertThrows(TestObjectTypeNotDetected.class, () -> {
            testUnknown();
        });
	}

	@Test
	public void test17_cache() throws URISyntaxException, IOException, TestObjectTypeNotDetected, ObjectWithIdNotFoundException, IncompatibleTestObjectTypeException {
		test11_Wfs20();
		test11_Wfs20();
		test12_Wfs11();
		test13_Wfs20SelectHighest();
		test13_Wfs20SelectHighest();
		boolean exceptionThrown=false;
		try {
            testUnknown();
		}catch(TestObjectTypeNotDetected e) {
			exceptionThrown=true;
		}
		assertTrue(exceptionThrown);
		test12_Wfs11();
		test11_Wfs20();
		test22_Wfs20FeatureCollectionFile();
		test15_incompatibleTypes();
		test16_incompatibleTypes();
	}

	@Test
	public void test21_CityGml20File() throws URISyntaxException, IOException, TestObjectTypeNotDetected {
		final IFile tmpDir = IFile.createTempDir("etf_junit");
		final IFile file = UriUtils.download(new URI("https://3d.bk.tudelft.nl/download/3dfier/Delft.gml.zip"));
		file.unzipTo(tmpDir);
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
				new LocalResource("dir",tmpDir));
		assertNotNull(detectedType);
		assertEquals("3e3639b1-f6b7-4d62-9160-963cfb2ea300", detectedType.getId().toString());
	}

	@Test
	public void test22_Wfs20FeatureCollectionFile() throws URISyntaxException, IOException, TestObjectTypeNotDetected {
		final IFile tmpDir = IFile.createTempDir("etf_junit");
		final IFile file = UriUtils.download(new URI("https://www.jherrmann.org/ps-ro-50.zip"));
		file.unzipTo(tmpDir);
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
				new LocalResource("dir",tmpDir));
		assertNotNull(detectedType);
		assertEquals("a8a1b437-0ebf-454c-8204-bcf0b8548d8c", detectedType.getId().toString());
	}

	@Test
	public void test23_Wfs20FeatureCollectionFileWithExpectedType() throws URISyntaxException, IOException, TestObjectTypeNotDetected,
			ObjectWithIdNotFoundException, IncompatibleTestObjectTypeException {
		final IFile tmpDir = IFile.createTempDir("etf_junit");
		final IFile file = UriUtils.download(new URI("https://www.jherrmann.org/ps-ro-50.zip"));
		file.unzipTo(tmpDir);
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
				new LocalResource("dir", tmpDir),
				TestObjectTypeDetectorManager.getTypes("a8a1b437-0ebf-454c-8204-bcf0b8548d8c").keySet());
		assertNotNull(detectedType);
		assertEquals("a8a1b437-0ebf-454c-8204-bcf0b8548d8c", detectedType.getId().toString());
	}

	public void test24_incompatibleTypes() throws URISyntaxException, IOException, TestObjectTypeNotDetected,
			ObjectWithIdNotFoundException, IncompatibleTestObjectTypeException {
		try {
			// Expecting GML FEATURE COLLECTION but provide WFS 2.0.0 URL
			final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
                create("https://services.interactive-instruments.de/cite-xs-46/simpledemo/cgi-bin/cities-postgresql/wfs"
									+ "?request=GetCapabilities&service=wfs"),
					TestObjectTypeDetectorManager.getTypes("e1d4a306-7a78-4a3b-ae2d-cf5f0810853e").keySet());
		}catch (final IncompatibleTestObjectTypeException e) {
			assertEquals("9b6ef734-981e-4d60-aa81-d6730a1c6389",
					e.getDetectedTestObjectType().getId().getId());
			return;
		}
		fail("Exception expected");
	}

	@Test
	public void test25_ShapeFile() throws URISyntaxException, IOException, TestObjectTypeNotDetected {
		final IFile tmpDir = IFile.createTempDir("etf_junit");
		final IFile file = UriUtils.download(new URI("http://www.adv-online.de/AdV-Produkte/Vertriebsstellen/ZSHH/binarywriterservlet?imgUid=417501de-a1a7-6651-8fda-7002072e13d6&uBasVariant=11111111-1111-1111-1111-111111111111"));
		file.unzipTo(tmpDir);
		final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(
				new LocalResource("dir",tmpDir));
		assertNotNull(detectedType);
		assertEquals("f91277ec-bbd9-49da-88ff-7b494f1f558d", detectedType.getId().toString());
	}

    @Test
    public void test26_OGC_API_Processes() throws URISyntaxException, IOException, TestObjectTypeNotDetected {
        final Resource apiProcesses = create("http://tb17.geolabs.fr:8081/ogc-api/");
        final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(apiProcesses);
        assertNotNull(detectedType);
        assertEquals("d576744d-95b1-374e-a19a-fdac61a2c226", detectedType.getId().toString());

        final TestObjectDto testObject = new TestObjectDto();
        testObject.addResource(new ResourceDto("serviceEndpoint", apiProcesses.getUri()));
        detectedType.enrichAndNormalize(testObject);

        assertEquals("The ZOO-Project OGC API - Processes Prototype Server", testObject.getLabel());
        assertEquals("Prototype version of the ZOO-Project OGC API - Processes made available for the "
            + "TestBed17. See http://www.zoo-project.org for more informations about the ZOO-Project.", testObject.getDescription());
        assertEquals(apiProcesses.getUri().toString(), testObject.getResourceByName("serviceEndpoint").toString());
    }

    @Test
    public void test27_OGC_API_Features() throws URISyntaxException, IOException, TestObjectTypeNotDetected {
        final Resource apiProcesses = create("https://weather.obs.fmibeta.com/");
        final DetectedTestObjectType detectedType = TestObjectTypeDetectorManager.detect(apiProcesses);
        assertNotNull(detectedType);
        assertEquals("63b1072e-90e6-317e-accb-3cd59d037e20", detectedType.getId().toString());

        final TestObjectDto testObject = new TestObjectDto();
        testObject.addResource(new ResourceDto("serviceEndpoint", apiProcesses.getUri()));
        detectedType.enrichAndNormalize(testObject);

        assertEquals("SOFP OGC API Features server", testObject.getLabel());
        assertNull(testObject.getDescription());
        assertEquals(apiProcesses.getUri().toString(), testObject.getResourceByName("serviceEndpoint").toString());
    }

}
