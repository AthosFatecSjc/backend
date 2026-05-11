db = db.getSiblingDB('admin');
db.auth('admin', 'admin123');

db = db.getSiblingDB('energia_analytics');

db.createUser({
  user: 'energia_app',
  pwd: 'app_password',
  roles: [
    { role: 'readWrite', db: 'energia_analytics' }
  ]
});

db.createCollection('interrupcoes');
db.createCollection('analises_tecnicas');
db.createCollection('dados_historicos');
db.createCollection('lgpd_user_registry');

db.interrupcoes.createIndex({ "concessionaria_id": 1, "data": -1 });
db.interrupcoes.createIndex({ "duracao": -1 });
db.analises_tecnicas.createIndex({ "data_analise": -1 });
db.lgpd_user_registry.createIndex({ "email": 1 }, { unique: true, sparse: true });
db.lgpd_user_registry.createIndex({ "deletedAt": 1 });
