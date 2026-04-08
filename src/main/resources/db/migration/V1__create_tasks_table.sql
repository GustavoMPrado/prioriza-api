CREATE TABLE IF NOT EXISTS tasks (
  id BIGSERIAL PRIMARY KEY,

  titulo VARCHAR(120) NOT NULL,
  descricao VARCHAR(500),

  status VARCHAR(255) NOT NULL DEFAULT 'A_FAZER',
  prioridade VARCHAR(255) NOT NULL DEFAULT 'MEDIA',

  data_limite DATE,

  criado_em TIMESTAMP NOT NULL,
  atualizado_em TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_prioridade ON tasks(prioridade);
CREATE INDEX IF NOT EXISTS idx_tasks_data_limite ON tasks(data_limite);