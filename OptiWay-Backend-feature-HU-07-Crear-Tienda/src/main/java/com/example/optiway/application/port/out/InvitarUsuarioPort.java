package com.example.optiway.application.port.out;

import java.util.UUID;

public interface InvitarUsuarioPort {

    UUID invitar(String email);
}