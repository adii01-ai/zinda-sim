package com.simkeeper.app.domain

/** Urgency. Colour means urgency and only urgency. */
enum class Status { SAFE, SOON, URGENT, EXPIRED }

/** Which of the two deadlines comes first. */
enum class Reason { PLAN, NO_USE }
