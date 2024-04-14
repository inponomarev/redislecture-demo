package edu.phystech;

import java.time.LocalDate;

public record Person(String name, String surname, LocalDate dateOfBirth) {
}
