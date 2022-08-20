package com.bignerdranch.android.moviegallery;

import org.junit.Test;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import static org.junit.Assert.*;

import com.bignerdranch.android.moviegallery.webrtc.model.IceCandidateDTO;
import com.bignerdranch.android.moviegallery.webrtc.model.SessionDescriptionDTO;
import com.google.gson.Gson;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void jsonPublicFieldsStringify() {
        Gson gson = new Gson();
        IceCandidate iceCandidate = new IceCandidate("sdpMid", 1, "sdp");
        String s = gson.toJson(iceCandidate);
        System.out.println("s = " + s);

    }

    @Test
    public void jsonPublicFieldsStringify2() {
        Gson gson = new Gson();
        SessionDescription object = new SessionDescription(SessionDescription.Type.OFFER, "description");
        String s = gson.toJson(object);
        System.out.println("s = " + s);

        SessionDescriptionDTO dto = gson.fromJson(s, SessionDescriptionDTO.class);
        System.out.println("dto = " + dto);

    }

    @Test
    public void jsonPublicFieldsParse() {
        String jsonStr = "{\"sdpMid\":\"sdpMid\",\"sdpMLineIndex\":1,\"sdp\":\"sdp\"}";
        Gson gson = new Gson();
        IceCandidateDTO iceCandidateDTO = gson.fromJson(jsonStr, IceCandidateDTO.class);
        System.out.println("iceCandidateDTO = " + iceCandidateDTO);

    }


}