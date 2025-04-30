/* Serial = inteiro e autoincrement */
CREATE TABLE IF NOT EXISTS reserva (
    id serial PRIMARY KEY,
    numero varchar(10) NOT NULL,
    nome varchar(100) NOT NULL,
    data date NOT NULL,
    hora time NOT NULL,
    duracao integer NOT NULL
);
        -- comando magico: ( drop schema public; )