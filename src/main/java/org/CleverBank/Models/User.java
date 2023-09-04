package org.CleverBank.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель пользователя, представляющая собой информацию о пользователе.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    /**
     * Уникальный идентификатор пользователя.
     */
    private int id;

    /**
     * Имя пользователя.
     */
    private String firstname;

    /**
     * Фамилия пользователя.
     */
    private String lastname;
}
