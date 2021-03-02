package at.htlgkr.prototype;

import static at.htlgkr.prototype.ContainsIgnoringWhitespace.containsIgnoringWhitespace;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource( locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PrototypeApplicationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    public void addedPatientShowsUpInTable() throws Exception {
        Patient patient = new Patient();
        patient.setId((long)1);
        patient.setFirstname("Test");
        patient.setLastname("User");
        patientRepository.save(patient);

        mvc.perform(get("/"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsIgnoringWhitespace("<tbody><tr><td><span>1</span></td><td><span>Test</span></td><td><span>User</span></td></tr></tbody>")));
    }

    @Test
    public void putAddsToDatabase() throws Exception {
        Patient patient = new Patient();
        patient.setFirstname("Test");
        patient.setLastname("User");
        Patient patient2 = new Patient();
        patient2.setFirstname("Test2");
        patient2.setLastname("User2");

        mvc.perform(post("/")
                .flashAttr("patient", patient))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsIgnoringWhitespace("<p>Added[1]TestUser</p>")));
        mvc.perform(post("/")
                .flashAttr("patient", patient2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsIgnoringWhitespace("<p>Added[2]Test2User2</p>")));

        Optional<Patient> p1 = patientRepository.findById((long)1);
        assertTrue(p1.isPresent());
        assertEquals((long)p1.get().getId(), 1);
        assertEquals(p1.get().getFirstname(), patient.getFirstname());
        assertEquals(p1.get().getLastname(), patient.getLastname());

        p1 = patientRepository.findById((long)2);
        assertTrue(p1.isPresent());
        assertEquals((long)p1.get().getId(), 2);
        assertEquals(p1.get().getFirstname(), patient2.getFirstname());
        assertEquals(p1.get().getLastname(), patient2.getLastname());
    }
}
