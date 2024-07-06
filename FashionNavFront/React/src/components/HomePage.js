// src/components/HomePage.js
import React from 'react';
import BannerSlider from './Slider';
import CategoryNews from './CategoryNews';

import './HomePage.css';
import LatestArticles from "./LatestArticles";

const HomePage = ({ isLoggedIn }) => {
    const categories = ['celeb', 'brand', 'trend'];

    return (
        <div className="home-page">
            <BannerSlider />
            {categories.map(category => (
                <CategoryNews key={category} category={category} isLoggedIn={isLoggedIn}/>
            ))}
            <LatestArticles /> {/* Add LatestNews component here */}
        </div>
    );
};

export default HomePage;
