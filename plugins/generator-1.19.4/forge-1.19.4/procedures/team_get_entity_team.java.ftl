(${input$entity} instanceof LivingEntity _teamEnt && _teamEnt.level.getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null ?
	_teamEnt.level.getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()).getName() : "")