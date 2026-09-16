UPDATE inventory_movements SET type = 'RECEIVE' WHERE type = 'RECEIPT';
UPDATE inventory_movements SET type = 'ADJUST' WHERE type = 'ADJUSTMENT';
