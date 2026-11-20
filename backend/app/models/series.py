from datetime import datetime, timezone
from sqlalchemy import String, DateTime, Text, Integer, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column, relationship
from typing import List, Optional
from app.models.base import Base


class Series(Base):
    __tablename__ = "series"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    title: Mapped[str] = mapped_column(String(255), nullable=False, index=True)
    slug: Mapped[str] = mapped_column(String(280), unique=True, index=True, nullable=False)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    cover_image: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    author_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        index=True
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        onupdate=lambda: datetime.now(timezone.utc)
    )

    # Relationships
    author = relationship("User", lazy="joined")
    items: Mapped[List["SeriesItem"]] = relationship(
        "SeriesItem",
        back_populates="series",
        cascade="all, delete-orphan",
        order_by="SeriesItem.order_index"
    )


class SeriesItem(Base):
    __tablename__ = "series_items"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    series_id: Mapped[int] = mapped_column(ForeignKey("series.id", ondelete="CASCADE"), index=True, nullable=False)
    post_id: Mapped[int] = mapped_column(ForeignKey("posts.id", ondelete="CASCADE"), index=True, nullable=False)
    order_index: Mapped[int] = mapped_column(Integer, default=0)

    series = relationship("Series", back_populates="items")
    post = relationship("Post", lazy="joined")
