package pl.tomaszstankowski.fourplayerchess.engine

data class StateFeatures(val attackedPositions: Map<Color, Set<Position>>,
                         val pins: Map<Color, List<Pin>>,
                         val checks: Map<Color, List<Check>>)

data class Check(val checkingPiecePosition: Position,
                 val checkedKingPosition: Position)

data class Pin(val pinningPiecePosition: Position, val pinnedPiecePosition: Position)