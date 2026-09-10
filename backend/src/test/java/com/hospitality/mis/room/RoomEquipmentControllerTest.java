package com.hospitality.mis.room;

import com.hospitality.mis.common.exception.GlobalExceptionHandler;
import com.hospitality.mis.controller.room.RoomEquipmentController;
import com.hospitality.mis.service.room.RoomEquipmentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class RoomEquipmentControllerTest {
    @Test
    void pathRoomMustMatchBodyRoom() throws Exception {
        RoomEquipmentService service = Mockito.mock(RoomEquipmentService.class);
        MockMvc mvc = standaloneSetup(new RoomEquipmentController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(post("/api/rooms/R101/equipment")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"room_id":"R102","name":"TV","original_value":1000000,
                                 "purchased_on":"2026-09-09","quantity":1}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ROOM_PATH_MISMATCH"));

        Mockito.verifyNoInteractions(service);
    }
}
